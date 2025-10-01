package com.geosat.gateway.service;

import com.geosat.gateway.client.RbmcHttpClient;
import com.geosat.gateway.model.RbmcFallbackResponse;
import com.geosat.gateway.model.RbmcArquivoDTO;
import com.geosat.gateway.model.RbmcRelatorioDTO;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.Duration;
import java.util.Map;

/**
 * Service placeholder: no futuro fará chamadas resilientes ao RBMC + cache Redis.
 */
@Service
public class RbmcService {

    private final RbmcHttpClient client;
    private final RedisCacheService cacheService;
    private final MeterRegistry meterRegistry;

    public RbmcService(RbmcHttpClient client, RedisCacheService cacheService, MeterRegistry meterRegistry) {
        this.client = client;
        this.cacheService = cacheService;
        this.meterRegistry = meterRegistry;
    }

    public Object obterRelatorio(String estacao) {
        String upper = estacao.toUpperCase();
        long start = System.nanoTime();
        try {
            client.obterRelatorio(upper); // Ignorando conteúdo binário por enquanto
            RbmcRelatorioDTO dto = new RbmcRelatorioDTO(
                    upper,
                    "pdf",
                    "https://servicodados.ibge.gov.br/api/v1/rbmc/relatorio/" + upper.toLowerCase(),
                    "Relatório técnico (placeholder)",
                    null,
                    Instant.now()
            );
            cacheService.putMetadata(upper, Map.of(
                    "link", dto.link(),
                    "tipo", dto.tipo(),
                    "ultimaAtualizacao", dto.ultimaAtualizacao().toString()
            ), Duration.ofHours(12));
            meterRegistry.counter("rbmc.requests.total", "method", "obterRelatorio", "status", "success").increment();
            meterRegistry.timer("rbmc.requests.duration", "method", "obterRelatorio").record(System.nanoTime() - start, java.util.concurrent.TimeUnit.NANOSECONDS);
            return dto;
        } catch (CallNotPermittedException cbOpen) {
            meterRegistry.counter("rbmc.requests.total", "method", "obterRelatorio", "status", "circuit_breaker").increment();
            meterRegistry.counter("rbmc.fallback.total").increment();
            return fallback(upper, "Circuit breaker aberto — a estação foi tomar um café ☕");
        } catch (Exception e) {
            meterRegistry.counter("rbmc.requests.total", "method", "obterRelatorio", "status", "error").increment();
            meterRegistry.counter("rbmc.fallback.total").increment();
            return fallback(upper, "Falha temporária — a estação tirou uma soneca 🚀");
        }
    }

    public Object obterRinex2(String estacao, int ano, int dia) {
        String upper = estacao.toUpperCase();
        String relative = "rinex2/" + upper.toLowerCase() + "/" + ano + "/" + dia;
        try {
            client.obterArquivo(relative); // placeholder
            RbmcArquivoDTO dto = new RbmcArquivoDTO(
                    upper,
                    "rinex2",
                    "15s",
                    link(relative),
                    "Arquivo RINEX2 diário (15s) placeholder",
                    Instant.now()
            );
            cacheService.putMetadata("RINEX2_" + upper + "_" + ano + "_" + dia, Map.of(
                    "link", dto.link(),
                    "intervalo", dto.intervalo()
            ), Duration.ofHours(6));
            return dto;
        } catch (CallNotPermittedException cbOpen) {
            return fallback(upper, "Circuit breaker aberto — não foi possível obter RINEX2");
        } catch (Exception e) {
            return fallback(upper, "Falha temporária ao obter RINEX2");
        }
    }

    public Object obterRinex3_1s(String estacao, int ano, int dia, int hora, int minuto, String tipo) {
        String upper = estacao.toUpperCase();
        String relative = "rinex3/1s/" + upper.toLowerCase() + "/" + ano + "/" + dia + "/" + hora + "/" + minuto + "/" + tipo.toLowerCase();
        try {
            client.obterArquivo(relative);
            RbmcArquivoDTO dto = new RbmcArquivoDTO(
                    upper,
                    "rinex3_1s",
                    "1s",
                    link(relative),
                    "Arquivo RINEX3 (1s) placeholder",
                    Instant.now()
            );
            cacheService.putMetadata("RINEX3_1S_" + upper + "_" + ano + "_" + dia + "_" + hora + "_" + minuto + "_" + tipo, Map.of(
                    "link", dto.link(),
                    "intervalo", dto.intervalo()
            ), Duration.ofHours(6));
            return dto;
        } catch (CallNotPermittedException cbOpen) {
            return fallback(upper, "Circuit breaker aberto — não foi possível obter RINEX3 1s");
        } catch (Exception e) {
            return fallback(upper, "Falha temporária ao obter RINEX3 1s");
        }
    }

    public Object obterRinex3_15s(String estacao, int ano, int dia) {
        String upper = estacao.toUpperCase();
        String relative = "rinex3/" + upper.toLowerCase() + "/" + ano + "/" + dia;
        try {
            client.obterArquivo(relative);
            RbmcArquivoDTO dto = new RbmcArquivoDTO(
                    upper,
                    "rinex3_15s",
                    "15s",
                    link(relative),
                    "Arquivo RINEX3 (15s) placeholder",
                    Instant.now()
            );
            cacheService.putMetadata("RINEX3_15S_" + upper + "_" + ano + "_" + dia, Map.of(
                    "link", dto.link(),
                    "intervalo", dto.intervalo()
            ), Duration.ofHours(6));
            return dto;
        } catch (CallNotPermittedException cbOpen) {
            return fallback(upper, "Circuit breaker aberto — não foi possível obter RINEX3 15s");
        } catch (Exception e) {
            return fallback(upper, "Falha temporária ao obter RINEX3 15s");
        }
    }

    public Object obterOrbitas(int ano, int dia) {
        String relative = "rinex3/orbitas/" + ano + "/" + dia;
        try {
            client.obterArquivo(relative);
            RbmcArquivoDTO dto = new RbmcArquivoDTO(
                    null,
                    "orbitas",
                    null,
                    link(relative),
                    "Órbitas multiconstelação placeholder",
                    Instant.now()
            );
            cacheService.putMetadata("ORBITAS_" + ano + "_" + dia, Map.of(
                    "link", dto.link()
            ), Duration.ofHours(12));
            return dto;
        } catch (CallNotPermittedException cbOpen) {
            return fallback("ORBITAS", "Circuit breaker aberto — não foi possível obter órbitas");
        } catch (Exception e) {
            return fallback("ORBITAS", "Falha temporária ao obter órbitas");
        }
    }

    private String link(String relative) {
        return "https://servicodados.ibge.gov.br/api/v1/rbmc/" + relative;
    }

    private RbmcFallbackResponse fallback(String estacao, String mensagem) {
        return new RbmcFallbackResponse(
                estacao,
                "indisponivel",
                mensagem,
                Instant.now(),
                cacheService.getMetadata(estacao).orElse(Map.of())
        );
    }
}
