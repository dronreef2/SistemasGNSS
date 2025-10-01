package com.geosat.gateway.controller;

import com.geosat.gateway.dto.RbmcSeriesRequest;
import com.geosat.gateway.model.*;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.validation.Valid;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

@RestController
@RequestMapping("/api/v1/estacoes")
@Validated
public class EstacaoController {

    private static final List<EstacaoDTO> ESTACOES = List.of(
            new EstacaoDTO("ALAR", "Alagoinhas", -12.135, -38.423, "ONLINE"),
            new EstacaoDTO("BRAZ", "Brasília", -15.793, -47.882, "ONLINE"),
            new EstacaoDTO("MANA", "Manaus", -3.118, -60.021, "OFFLINE"),
            new EstacaoDTO("POAL", "Porto Alegre", -30.027, -51.228, "ONLINE")
    );

    private final MeterRegistry meterRegistry;

    public EstacaoController(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    @GetMapping
    public List<EstacaoDTO> listar() {
        // Deriva status rápido: hash define offset de minutos; > 120 min => OFFLINE
        List<EstacaoDTO> out = new ArrayList<>();
        for(EstacaoDTO e: ESTACOES){
            long offsetMin = (Math.abs(Objects.hash(e.codigo())) % 900) + 3;
            String status = offsetMin > 120 ? "OFFLINE" : "ONLINE";
            out.add(new EstacaoDTO(e.codigo(), e.nome(), e.latitude(), e.longitude(), status));
        }
        return out;
    }

    @GetMapping(value = "/geojson", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String,Object> geojson(){
        Map<String,Object> fc = new LinkedHashMap<>();
        fc.put("type", "FeatureCollection");
        List<Map<String,Object>> features = new ArrayList<>();
        for(EstacaoDTO e: ESTACOES){
            Map<String,Object> f = new LinkedHashMap<>();
            f.put("type","Feature");
            Map<String,Object> geom = new LinkedHashMap<>();
            geom.put("type","Point");
            geom.put("coordinates", List.of(e.longitude(), e.latitude()));
            f.put("geometry", geom);
            Map<String,Object> props = new LinkedHashMap<>();
            props.put("codigo", e.codigo());
            props.put("nome", e.nome());
            props.put("status", e.status());
            f.put("properties", props);
            features.add(f);
        }
        fc.put("features", features);
        return fc;
    }

    @GetMapping("/{codigo}/metadados")
    public MetadadosEstacaoDTO metadados(@PathVariable("codigo") String codigo){
        // Placeholder: última observação 3–900 minutos atrás de forma pseudo-randômica
        long offsetMin = (Math.abs(Objects.hash(codigo)) % 900) + 3; // entre 3 e 903 min
        Instant ultima = Instant.now().minusSeconds(offsetMin * 60);
        return new MetadadosEstacaoDTO(codigo.toUpperCase(), "TRIMBLE NETR9", "TRM59800.00", 1.234,
                DateTimeFormatter.ISO_INSTANT.format(ultima));
    }

    @GetMapping("/{codigo}/snr")
    public ResponseEntity<SnrSerieDTO> snr(
            @PathVariable("codigo") String codigo,
            @Valid @ModelAttribute RbmcSeriesRequest request) {
        
        // Usar valores do request validado
        int ano = request.getAno();
        int dia = request.getDia();
        int max = request.getMax() != null ? request.getMax() : 300;
        
        int rawPoints = 1440; // 1 ponto por minuto do dia
        List<SnrSampleDTO> raw = new ArrayList<>(rawPoints);
        Instant base = Instant.parse(ano + "-01-01T00:00:00Z").plusSeconds((long)(dia-1) * 86400L);
        ThreadLocalRandom r = ThreadLocalRandom.current();
        for(int i=0;i<rawPoints;i++){
            Instant t = base.plusSeconds(i * 60L);
            raw.add(new SnrSampleDTO(t.toString(), "G"+String.format("%02d", (i%28)+1), 25 + r.nextDouble()*25));
        }
    List<SnrSampleDTO> decimated = decimateSnr(raw, max);
    meterRegistry.counter("estacoes.snr.decimations", "codigo", codigo.toUpperCase()).increment();
    meterRegistry.gauge("estacoes.snr.size.before", java.util.Collections.emptyList(), raw.size());
    meterRegistry.gauge("estacoes.snr.size.after", java.util.Collections.emptyList(), decimated.size());
    return ResponseEntity.ok()
        .cacheControl(CacheControl.maxAge(java.time.Duration.ofSeconds(30)).cachePublic())
        .body(new SnrSerieDTO(codigo.toUpperCase(), ano, dia, decimated));
    }

    @GetMapping("/{codigo}/posicoes")
    public ResponseEntity<PosicaoSerieDTO> posicoes(
            @PathVariable("codigo") String codigo,
            @Valid @ModelAttribute RbmcSeriesRequest request) {
        
        // Usar valores do request validado
        int ano = request.getAno();
        int dia = request.getDia();
        int max = request.getMax() != null ? request.getMax() : 300;
        
        int rawPoints = 2880; // 30s step
        EstacaoDTO baseEst = ESTACOES.stream().filter(e->e.codigo().equalsIgnoreCase(codigo)).findFirst().orElse(ESTACOES.get(0));
        double lat = baseEst.latitude();
        double lon = baseEst.longitude();
        ThreadLocalRandom rand = ThreadLocalRandom.current();
        Instant base = Instant.parse(ano + "-01-01T00:00:00Z").plusSeconds((long)(dia-1) * 86400L);
        List<PosicaoSampleDTO> raw = new ArrayList<>(rawPoints);
        for(int i=0;i<rawPoints;i++){
            Instant t = base.plusSeconds(i*30L);
            raw.add(new PosicaoSampleDTO(t.toString(), lat + rand.nextDouble(-0.0005,0.0005), lon + rand.nextDouble(-0.0005,0.0005), 400 + rand.nextDouble(-2,2)));
        }
    List<PosicaoSampleDTO> decimated = decimatePos(raw, max);
    meterRegistry.counter("estacoes.pos.decimations", "codigo", codigo.toUpperCase()).increment();
    meterRegistry.gauge("estacoes.pos.size.before", java.util.Collections.emptyList(), raw.size());
    meterRegistry.gauge("estacoes.pos.size.after", java.util.Collections.emptyList(), decimated.size());
    return ResponseEntity.ok()
        .cacheControl(CacheControl.maxAge(java.time.Duration.ofSeconds(30)).cachePublic())
        .body(new PosicaoSerieDTO(codigo.toUpperCase(), ano, dia, "WGS84", decimated));
    }

    private List<SnrSampleDTO> decimateSnr(List<SnrSampleDTO> raw, int max){
        if(raw.size() <= max) return raw;
        double step = (double) raw.size() / max;
        List<SnrSampleDTO> out = new ArrayList<>(max);
        for(int i=0;i<max;i++){
            int idx = (int)Math.floor(i*step);
            out.add(raw.get(idx));
        }
        return out;
    }

    private List<PosicaoSampleDTO> decimatePos(List<PosicaoSampleDTO> raw, int max){
        if(raw.size() <= max) return raw;
        double step = (double) raw.size() / max;
        List<PosicaoSampleDTO> out = new ArrayList<>(max);
        for(int i=0;i<max;i++){
            int idx = (int)Math.floor(i*step);
            out.add(raw.get(idx));
        }
        return out;
    }
}
