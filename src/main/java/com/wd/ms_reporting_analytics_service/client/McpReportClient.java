package com.wd.ms_reporting_analytics_service.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.wd.ms_reporting_analytics_service.dto.AiReportRequest;
import com.wd.ms_reporting_analytics_service.dto.AiReportResponse;

<<<<<<< HEAD
/**
 * Cliente hacia el MCP de Python. A diferencia de EventCategoryClient/
 * ScoringClient (que son @FeignClient porque esos servicios estan
 * registrados en Consul), este usa RestClient plano apuntando a una
 * URL configurada directamente en application.yml (ai.mcp.report-endpoint),
 * porque el MCP no es un microservicio Spring del ecosistema World Dance.
 *
 * OJO: la URL en application.yml es un placeholder
 * (http://localhost:8000/generate-report-narrative) hasta que el
 * endpoint real del MCP este listo - ajustar ahi, no aqui.
 */
@Component
=======
import lombok.extern.slf4j.Slf4j;

/**
 * Cliente hacia el MCP de Python con resiliencia y fallback automatico.
 */
@Component
@Slf4j
>>>>>>> develop
public class McpReportClient {

    private final RestClient restClient;
    private final String reportEndpoint;

<<<<<<< HEAD
    public McpReportClient(@Value("${ai.mcp.report-endpoint}") String reportEndpoint) {
        this.reportEndpoint = reportEndpoint;
        this.restClient = RestClient.create();
    }

    public AiReportResponse generateNarrative(AiReportRequest request) {
        return restClient.post()
                .uri(reportEndpoint)
                .body(request)
                .retrieve()
                .body(AiReportResponse.class);
=======
    public McpReportClient(@Value("${ai.mcp.report-endpoint:http://localhost:8000/generate-report-narrative}") String reportEndpoint) {
        this.reportEndpoint = reportEndpoint;
        this.restClient = RestClient.builder().build();
    }

    public AiReportResponse generateNarrative(AiReportRequest request) {
        try {
            log.info("Llamando al MCP de IA en endpoint: {}", reportEndpoint);
            AiReportResponse response = restClient.post()
                    .uri(reportEndpoint)
                    .body(request)
                    .retrieve()
                    .body(AiReportResponse.class);

            if (response != null && response.getIntroduccion() != null) {
                return response;
            }
        } catch (Exception e) {
            log.warn("No se pudo conectar con el MCP de IA ({}): {}. Generando narrativa estandar de respaldo.", reportEndpoint, e.getMessage());
        }

        return createFallbackNarrative(request);
    }

    private AiReportResponse createFallbackNarrative(AiReportRequest request) {
        AiReportResponse fallback = new AiReportResponse();
        String eventName = request.getEventName() != null ? request.getEventName() : "Evento World Dance";

        fallback.setIntroduccion(String.format(
                "El evento '%s' se ha ejecutado satisfactoriamente con un total de %d inscripciones aprobadas y %d modalidades estructuradas.",
                eventName, request.getTotalParticipants(), request.getTotalModalities()
        ));

        fallback.setAnalisisPorModalidad(String.format(
                "Durante el juzgamiento de las modalidades, se registro un promedio general de %.2f puntos. La puntuacion mas alta obtenida fue de %.2f y la mas baja de %.2f.",
                request.getOverallAverageScore(), request.getHighestScore(), request.getLowestScore()
        ));

        fallback.setConclusion(
                "El balance del evento refleja una participacion activa y un proceso de evaluacion consolidado. Este reporte consolida los datos cuantitativos y metricas operativas del evento."
        );

        return fallback;
>>>>>>> develop
    }
}