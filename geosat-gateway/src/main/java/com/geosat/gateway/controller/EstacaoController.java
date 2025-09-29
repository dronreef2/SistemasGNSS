package com.geosat.gateway.controller;

import com.geosat.gateway.model.*;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

@RestController
@RequestMapping("/api/v1/estacoes")
public class EstacaoController {

    private static final List<EstacaoDTO> ESTACOES = List.of(
            new EstacaoDTO("ALAR", "Alagoinhas", -12.135, -38.423, "ONLINE"),
            new EstacaoDTO("BRAZ", "Brasília", -15.793, -47.882, "ONLINE"),
            new EstacaoDTO("MANA", "Manaus", -3.118, -60.021, "OFFLINE"),
            new EstacaoDTO("POAL", "Porto Alegre", -30.027, -51.228, "ONLINE")
    );

    @GetMapping
    public List<EstacaoDTO> listar() { return ESTACOES; }

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
    public MetadadosEstacaoDTO metadados(@PathVariable String codigo){
        // Placeholder simples
        return new MetadadosEstacaoDTO(codigo.toUpperCase(), "TRIMBLE NETR9", "TRM59800.00", 1.234,
                DateTimeFormatter.ISO_INSTANT.format(Instant.now().minusSeconds(600)));
    }

    @GetMapping("/{codigo}/snr")
    public SnrSerieDTO snr(@PathVariable String codigo, @RequestParam int ano, @RequestParam int dia){
        int points = 120; // placeholder
        List<SnrSampleDTO> list = new ArrayList<>(points);
        Instant base = Instant.parse(ano + "-01-01T00:00:00Z").plusSeconds((long)(dia-1) * 86400L);
        ThreadLocalRandom r = ThreadLocalRandom.current();
        for(int i=0;i<points;i++){
            Instant t = base.plusSeconds(i* (86400L/points));
            list.add(new SnrSampleDTO(t.toString(), "G"+String.format("%02d", (i%10)+1), 30 + r.nextDouble()*20));
        }
        return new SnrSerieDTO(codigo.toUpperCase(), ano, dia, list);
    }

    @GetMapping("/{codigo}/posicoes")
    public PosicaoSerieDTO posicoes(@PathVariable String codigo, @RequestParam int ano, @RequestParam int dia){
        int points = 120;
        List<PosicaoSampleDTO> list = new ArrayList<>(points);
        EstacaoDTO baseEst = ESTACOES.stream().filter(e->e.codigo().equalsIgnoreCase(codigo)).findFirst().orElse(ESTACOES.get(0));
        double lat = baseEst.latitude();
        double lon = baseEst.longitude();
        ThreadLocalRandom rand = ThreadLocalRandom.current();
        Instant base = Instant.parse(ano + "-01-01T00:00:00Z").plusSeconds((long)(dia-1) * 86400L);
        for(int i=0;i<points;i++){
            Instant t = base.plusSeconds(i* (86400L/points));
            list.add(new PosicaoSampleDTO(t.toString(), lat + rand.nextDouble(-0.0005,0.0005), lon + rand.nextDouble(-0.0005,0.0005), 400 + rand.nextDouble(-2,2)));
        }
        return new PosicaoSerieDTO(codigo.toUpperCase(), ano, dia, "WGS84", list);
    }
}
