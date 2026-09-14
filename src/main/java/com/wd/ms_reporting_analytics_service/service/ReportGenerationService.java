package com.wd.ms_reporting_analytics_service.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;

import com.wd.ms_reporting_analytics_service.client.McpReportClient;
import com.wd.ms_reporting_analytics_service.domain.EventSummary;
import com.wd.ms_reporting_analytics_service.dto.AiReportRequest;
import com.wd.ms_reporting_analytics_service.dto.AiReportResponse;
import com.wd.ms_reporting_analytics_service.repository.EventSummaryRepository;

import lombok.RequiredArgsConstructor;

/**
 * RF-62: orquesta la generacion completa del reporte (PDF o Excel).
 *
 * Flujo:
 *   1. Trae el EventSummary consolidado (si no existe, lo sincroniza
 *      primero llamando a EventSummaryConsolidationService)
 *   2. Arma el AiReportRequest con las metricas
 *   3. Llama al MCP de Python -> recibe la narrativa (AiReportResponse)
 *   4. Deja todo listo en un objeto ReportData que usan tanto
 *      PdfGeneratorService como ExcelGeneratorService
 */
@Service
@RequiredArgsConstructor
public class ReportGenerationService {

    private final EventSummaryRepository eventSummaryRepository;
    private final EventSummaryConsolidationService consolidationService;
    private final McpReportClient mcpReportClient;

    public record ReportData(EventSummary summary, AiReportResponse narrative) {}

    public ReportData buildReportData(Long eventId) {
        String eventIdStr = String.valueOf(eventId);

        EventSummary summary = eventSummaryRepository.findByEventId(eventIdStr)
                .orElseGet(() -> consolidationService.syncEventSummary(eventId));

        AiReportRequest aiRequest = toAiRequest(summary);
        AiReportResponse narrative = new AiReportResponse();
        try {
            narrative = mcpReportClient.generateNarrative(aiRequest);
        } catch (Exception e) {
            // Fallback si el microservicio de python MCP no esta corriendo
            narrative.setIntroduccion("");
            narrative.setAnalisisPorModalidad("");
            narrative.setConclusion("");
        }

        return new ReportData(summary, narrative);
    }

    /** Contexto listo para pasarle directo a la plantilla Thymeleaf. */
    public Context toTemplateContext(ReportData data) {
        Context context = new Context();
        EventSummary summary = data.summary();
        AiReportResponse narrative = data.narrative();

        context.setVariable("eventName", summary.getEventName());
        context.setVariable("executionDate", summary.getExecutionDate());
        context.setVariable("totalParticipants", summary.getTotals().getApprovedEnrollments());
        context.setVariable("totalModalities", summary.getTotals().getTotalModalities());
        context.setVariable("overallAverageScore", String.format("%.2f", summary.getEvaluationMetrics().getOverallAverage()));
        context.setVariable("highestScore", String.format("%.2f", summary.getEvaluationMetrics().getHighestScore()));
        context.setVariable("lowestScore", String.format("%.2f", summary.getEvaluationMetrics().getLowestScore()));
        
        // Cronograma metrics
        context.setVariable("totalScheduledSlots", summary.getScheduleMetrics() != null ? summary.getScheduleMetrics().getTotalSlots() : 0);
        context.setVariable("scheduleStatus", summary.getScheduleMetrics() != null ? summary.getScheduleMetrics().getScheduleStatus() : "N/A");

        // Format modality average scores
        if (summary.getModalitiesBreakdown() != null) {
            summary.getModalitiesBreakdown().forEach(m -> {
                if (m.getAverageScore() != null) {
                    m.setAverageScore(Math.round(m.getAverageScore() * 100.0) / 100.0);
                }
            });
        }
        context.setVariable("modalities", summary.getModalitiesBreakdown());
        context.setVariable("introduccion", narrative.getIntroduccion());
        context.setVariable("analisisPorModalidad", narrative.getAnalisisPorModalidad());
        context.setVariable("conclusion", narrative.getConclusion());

        return context;
    }

    private AiReportRequest toAiRequest(EventSummary summary) {
        List<AiReportRequest.ModalityStat> modalityStats = summary.getModalitiesBreakdown() == null
                ? List.of()
                : summary.getModalitiesBreakdown().stream()
                    .map(m -> new AiReportRequest.ModalityStat(
                            m.getCategory(), m.getDivision(), m.getParticipantCount(), m.getAverageScore()))
                    .toList();

        return new AiReportRequest(
                summary.getEventName(),
                summary.getTotals().getApprovedEnrollments(),
                summary.getTotals().getTotalModalities(),
                summary.getEvaluationMetrics().getOverallAverage(),
                summary.getEvaluationMetrics().getHighestScore(),
                summary.getEvaluationMetrics().getLowestScore(),
                modalityStats);
    }
}
