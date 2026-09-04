package com.wd.ms_reporting_analytics_service.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.wd.ms_reporting_analytics_service.dto.AiReportRequest;
import com.wd.ms_reporting_analytics_service.dto.AiReportResponse;

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
public class McpReportClient {

    private final RestClient restClient;
    private final String reportEndpoint;

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
    }
}