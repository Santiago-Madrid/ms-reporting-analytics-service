package com.wd.ms_reporting_analytics_service.service;

import java.time.Instant;
import java.util.List;

import org.springframework.stereotype.Service;

import com.wd.ms_reporting_analytics_service.client.EventCategoryClient;
import com.wd.ms_reporting_analytics_service.client.EventClient;
import com.wd.ms_reporting_analytics_service.client.ScoringClient;
import com.wd.ms_reporting_analytics_service.domain.EventSummary;
import com.wd.ms_reporting_analytics_service.dto.external.EventResponseDto;
import com.wd.ms_reporting_analytics_service.dto.external.ModalityResponseDto;
import com.wd.ms_reporting_analytics_service.dto.external.ResultResponse;
import com.wd.ms_reporting_analytics_service.repository.EventSummaryRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Orquesta la actualizacion del read model event_summaries (punto B que
 * elegiste: endpoint interno en vez de mensajeria).
 *
 * Flujo:
 *   1. ms-event-category (EventClient) -> nombre, fecha y ownerId del evento
 *   2. ms-event-category (EventCategoryClient) -> lista de modalidades (Long ids)
 *   3. Por cada modalidad -> ms-scoring -> resultados (String ids)
 *   4. Se calcula participant_count / average_score por modalidad
 *   5. Se calculan evaluation_metrics globales del evento
 *   6. Se hace upsert del documento EventSummary (por eventId)
 *
 * NOTA sobre resiliencia: si ms-scoring, ms-event-category estan caidos,
 * hoy esto lanzara la excepcion de Feign tal cual. Cuando quieras, en un
 * siguiente paso le agregamos manejo de errores / circuit breaker para
 * que una modalidad fallida no tumbe todo el consolidado.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class EventSummaryConsolidationService {

    private final EventCategoryClient eventCategoryClient;
    private final EventClient eventClient;
    private final ScoringClient scoringClient;
    private final EventSummaryRepository eventSummaryRepository;

    public EventSummary syncEventSummary(Long eventId) {
        String eventIdStr = String.valueOf(eventId);

        EventResponseDto event = eventClient.getEventById(eventId).getData();

        List<ModalityResponseDto> modalities = eventCategoryClient
                .getModalitiesByEventId(eventId)
                .getData();

        EventSummary summary = eventSummaryRepository.findByEventId(eventIdStr)
                .orElseGet(EventSummary::new);
        summary.setEventId(eventIdStr);
        summary.setOwnerId(event.getOwnerId());
        summary.setEventName(event.getName());
        summary.setExecutionDate(event.getStartDate());

        List<EventSummary.ModalityBreakdown> breakdowns = new java.util.ArrayList<>();

        int totalParticipants = 0;
        double sumOfAllScores = 0.0;
        int countOfAllScores = 0;
        double highest = Double.MIN_VALUE;
        double lowest = Double.MAX_VALUE;

        for (ModalityResponseDto modality : modalities) {
            List<ResultResponse> results = scoringClient.getResultsByModality(
                    eventIdStr,
                    String.valueOf(modality.getId()));

            EventSummary.ModalityBreakdown breakdown = new EventSummary.ModalityBreakdown();
            breakdown.setModalityId(modality.getId());
            breakdown.setCategory(modality.getCategory());
            breakdown.setDivision(modality.getDivision());
            breakdown.setParticipantCount(results.size());

            double avg = results.stream()
                    .filter(r -> r.getFinalScore() != null)
                    .mapToDouble(ResultResponse::getFinalScore)
                    .average()
                    .orElse(0.0);
            breakdown.setAverageScore(avg);

            breakdowns.add(breakdown);

            totalParticipants += results.size();
            for (ResultResponse r : results) {
                if (r.getFinalScore() == null) continue;
                sumOfAllScores += r.getFinalScore();
                countOfAllScores++;
                highest = Math.max(highest, r.getFinalScore());
                lowest = Math.min(lowest, r.getFinalScore());
            }
        }

        summary.setModalitiesBreakdown(breakdowns);
        summary.getTotals().setTotalModalities(modalities.size());
        summary.getTotals().setApprovedEnrollments(totalParticipants);

        summary.getEvaluationMetrics().setOverallAverage(
                countOfAllScores > 0 ? sumOfAllScores / countOfAllScores : 0.0);
        summary.getEvaluationMetrics().setHighestScore(
                countOfAllScores > 0 ? highest : 0.0);
        summary.getEvaluationMetrics().setLowestScore(
                countOfAllScores > 0 ? lowest : 0.0);

        summary.setGeneratedAt(Instant.now());

        return eventSummaryRepository.save(summary);
    }
}
