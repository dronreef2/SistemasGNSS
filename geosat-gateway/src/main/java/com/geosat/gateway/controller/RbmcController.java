package com.geosat.gateway.controller;

import com.geosat.gateway.dto.RbmcSeriesRequest;
import com.geosat.gateway.model.RbmcRelatorioDTO;
import com.geosat.gateway.model.RbmcFallbackResponse;
import com.geosat.gateway.model.RbmcArquivoDTO;
import com.geosat.gateway.service.RbmcService;
import com.geosat.gateway.service.CircuitBreakerStateService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

@RestController
@RequestMapping("/api/v1/rbmc")
@Validated
public class RbmcController {

        private final RbmcService service;
        private final CircuitBreakerStateService cbState;

        public RbmcController(RbmcService service, CircuitBreakerStateService cbState) {
                this.service = service;
                this.cbState = cbState;
    }

    @GetMapping("/{estacao}/relatorio")
    @Operation(summary = "Obtém relatório RBMC (ou fallback)", description = "Retorna metadados do relatório da estação ou, em caso de indisponibilidade, um objeto de fallback com cache.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Relatório disponível",
                    content = @Content(schema = @Schema(implementation = RbmcRelatorioDTO.class)) ),
            @ApiResponse(responseCode = "400", description = "Parâmetros inválidos", content = @Content),
            @ApiResponse(responseCode = "503", description = "Serviço remoto indisponível (fallback)",
                    content = @Content(schema = @Schema(implementation = RbmcFallbackResponse.class)))
    })
    public ResponseEntity<Object> getRelatorio(
            @PathVariable("estacao") @Pattern(regexp = "^[A-Za-z]{4}$", message = "Estacao deve ter 4 letras") String estacao) {
        Object result = service.obterRelatorio(estacao);
                if (result instanceof RbmcFallbackResponse fb) {
                        return withRetryAfterIfOpen(fb);
        }
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "Obtém arquivo RINEX2 (15s)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "RINEX2 disponível",
                    content = @Content(schema = @Schema(implementation = RbmcArquivoDTO.class))),
            @ApiResponse(responseCode = "503", description = "Fallback", content = @Content(schema = @Schema(implementation = RbmcFallbackResponse.class)))
    })
    @GetMapping("/rinex2/{estacao}/{ano}/{dia}")
    public ResponseEntity<Object> getRinex2(
            @PathVariable("estacao") @Pattern(regexp = "^[A-Za-z]{4}$") String estacao,
            @PathVariable("ano") @Pattern(regexp = "^\\d{4}$") String ano,
            @PathVariable("dia") @Pattern(regexp = "^\\d{1,3}$") String dia) {
        Object result = service.obterRinex2(estacao, Integer.parseInt(ano), Integer.parseInt(dia));
                if (result instanceof RbmcFallbackResponse fb) {
                        return withRetryAfterIfOpen(fb);
        }
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "Obtém arquivo RINEX3 (1s)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "RINEX3 1s disponível",
                    content = @Content(schema = @Schema(implementation = RbmcArquivoDTO.class))),
            @ApiResponse(responseCode = "503", description = "Fallback", content = @Content(schema = @Schema(implementation = RbmcFallbackResponse.class)))
    })
    @GetMapping("/rinex3/1s/{estacao}/{ano}/{dia}/{hora}/{minuto}/{tipo}")
    public ResponseEntity<Object> getRinex3_1s(
            @PathVariable("estacao") @Pattern(regexp = "^[A-Za-z]{4}$") String estacao,
            @PathVariable("ano") @Pattern(regexp = "^\\d{4}$") String ano,
            @PathVariable("dia") @Pattern(regexp = "^\\d{1,3}$") String dia,
            @PathVariable("hora") @Pattern(regexp = "^([01]?\\d|2[0-3])$") String hora,
            @PathVariable("minuto") @Pattern(regexp = "^(0|15|30|45)$") String minuto,
            @PathVariable("tipo") @Pattern(regexp = "(?i)^(MO|MN)$") String tipo) {
        Object result = service.obterRinex3_1s(estacao, Integer.parseInt(ano), Integer.parseInt(dia), Integer.parseInt(hora), Integer.parseInt(minuto), tipo);
                if (result instanceof RbmcFallbackResponse fb) {
                        return withRetryAfterIfOpen(fb);
        }
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "Obtém arquivo RINEX3 (15s)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "RINEX3 15s disponível",
                    content = @Content(schema = @Schema(implementation = RbmcArquivoDTO.class))),
            @ApiResponse(responseCode = "503", description = "Fallback", content = @Content(schema = @Schema(implementation = RbmcFallbackResponse.class)))
    })
    @GetMapping("/rinex3/{estacao}/{ano}/{dia}")
    public ResponseEntity<Object> getRinex3_15s(
            @PathVariable("estacao") @Pattern(regexp = "^[A-Za-z]{4}$") String estacao,
            @PathVariable("ano") @Pattern(regexp = "^\\d{4}$") String ano,
            @PathVariable("dia") @Pattern(regexp = "^\\d{1,3}$") String dia) {
        Object result = service.obterRinex3_15s(estacao, Integer.parseInt(ano), Integer.parseInt(dia));
                if (result instanceof RbmcFallbackResponse fb) {
                        return withRetryAfterIfOpen(fb);
        }
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "Obtém órbitas multiconstelação")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Órbitas disponíveis",
                    content = @Content(schema = @Schema(implementation = RbmcArquivoDTO.class))),
            @ApiResponse(responseCode = "503", description = "Fallback", content = @Content(schema = @Schema(implementation = RbmcFallbackResponse.class)))
    })
    @GetMapping("/rinex3/orbitas/{ano}/{dia}")
    public ResponseEntity<Object> getOrbitas(
            @PathVariable("ano") @Pattern(regexp = "^\\d{4}$") String ano,
            @PathVariable("dia") @Pattern(regexp = "^\\d{1,3}$") String dia) {
        Object result = service.obterOrbitas(Integer.parseInt(ano), Integer.parseInt(dia));
                if (result instanceof RbmcFallbackResponse fb) {
                        return withRetryAfterIfOpen(fb);
        }
        return ResponseEntity.ok(result);
    }

        private ResponseEntity<Object> withRetryAfterIfOpen(RbmcFallbackResponse fb){
                var builder = ResponseEntity.status(503);
                cbState.remainingOpenSeconds().ifPresent(secs -> builder.header("Retry-After", String.valueOf(secs)));
                return builder.body(fb);
        }

    // Outros endpoints serão adicionados em PRs futuros
}
