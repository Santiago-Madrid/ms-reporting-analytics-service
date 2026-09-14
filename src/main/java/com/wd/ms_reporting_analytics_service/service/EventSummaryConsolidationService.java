package com.wd.ms_reporting_analytics_service.service;

import java.time.Instant;
<<<<<<< HEAD
=======
import java.util.ArrayList;
>>>>>>> develop
import java.util.List;

import org.springframework.stereotype.Service;

import com.wd.ms_reporting_analytics_service.client.EventCategoryClient;
import com.wd.ms_reporting_analytics_service.client.EventClient;
<<<<<<< HEAD
import com.wd.ms_reporting_analytics_service.client.ScoringClient;
import com.wd.ms_reporting_analytics_service.domain.EventSummary;
import com.wd.ms_reporting_analytics_service.dto.external.EventResponseDto;
import com.wd.ms_reporting_analytics_service.dto.external.ModalityResponseDto;
import com.wd.ms_reporting_analytics_service.dto.external.ResultResponse;
=======
import com.wd.ms_reporting_analytics_service.client.SchedulingClient;
import com.wd.ms_reporting_analytics_service.client.ScoringClient;
import com.wd.ms_reporting_analytics_service.domain.EventSummary;
import com.wd.ms_reporting_analytics_service.dto.external.EventResponseDto;
import com.wd.ms_reporting_analytics_service.dto.external.HttpGlobalResponse;
import com.wd.ms_reporting_analytics_service.dto.external.ModalityResponseDto;
import com.wd.ms_reporting_analytics_service.dto.external.ResultResponse;
import com.wd.ms_reporting_analytics_service.dto.external.ScheduleResponseDto;
>>>>>>> develop
import com.wd.ms_reporting_analytics_service.repository.EventSummaryRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
<<<<<<< HEAD
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
=======
 * Orquesta la actualizacion del read model event_summaries con resiliencia.
>>>>>>> develop
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class EventSummaryConsolidationService {

    private final EventCategoryClient eventCategoryClient;
    private final EventClient eventClient;
    private final ScoringClient scoringClient;
<<<<<<< HEAD
=======
    private final SchedulingClient schedulingClient;
>>>>>>> develop
    private final EventSummaryRepository eventSummaryRepository;

    public EventSummary syncEventSummary(Long eventId) {
        String eventIdStr = String.valueOf(eventId);

<<<<<<< HEAD
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
=======
        EventSummary summary = eventSummaryRepository.findByEventId(eventIdStr)
                .orElseGet(EventSummary::new);
        summary.setEventId(eventIdStr);

        // 1. Obtener datos basicos del evento
        try {
            HttpGlobalResponse<EventResponseDto> eventResp = eventClient.getEventById(eventId);
            if (eventResp != null && eventResp.getData() != null) {
                EventResponseDto event = eventResp.getData();
                summary.setOwnerId(event.getOwnerId());
                summary.setEventName(event.getName());
                summary.setExecutionDate(event.getStartDate());
            }
        } catch (Exception e) {
            log.warn("No se pudo obtener informacion del evento {} desde ms-event-category: {}", eventId, e.getMessage());
            if (summary.getEventName() == null) {
                summary.setEventName("Evento #" + eventId);
            }
        }

        // 2. Obtener modalidades
        List<ModalityResponseDto> modalities = new ArrayList<>();
        try {
            HttpGlobalResponse<List<ModalityResponseDto>> modResp = eventCategoryClient.getModalitiesByEventId(eventId);
            if (modResp != null && modResp.getData() != null) {
                modalities = modResp.getData();
            }
        } catch (Exception e) {
            log.warn("No se pudieron obtener las modalidades del evento {} desde ms-event-category: {}", eventId, e.getMessage());
        }

        // 3. Obtener resultados de juzgamiento desde ms-scoring
        List<EventSummary.ModalityBreakdown> breakdowns = new ArrayList<>();
        int totalParticipants = 0;
        double sumOfAllScores = 0.0;
        int countOfAllScores = 0;
        double highest = 0.0;
        double lowest = 0.0;
        boolean hasAnyScore = false;

        for (ModalityResponseDto modality : modalities) {
            List<ResultResponse> results = new ArrayList<>();
            try {
                results = scoringClient.getResultsByModality(eventIdStr, String.valueOf(modality.getId()));
                if (results == null) {
                    results = new ArrayList<>();
                }
            } catch (Exception e) {
                log.warn("No se pudieron obtener resultados para modalidad {} del evento {}: {}", modality.getId(), eventId, e.getMessage());
            }
>>>>>>> develop

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
<<<<<<< HEAD

=======
>>>>>>> develop
            breakdowns.add(breakdown);

            totalParticipants += results.size();
            for (ResultResponse r : results) {
                if (r.getFinalScore() == null) continue;
<<<<<<< HEAD
                sumOfAllScores += r.getFinalScore();
                countOfAllScores++;
                highest = Math.max(highest, r.getFinalScore());
                lowest = Math.min(lowest, r.getFinalScore());
=======
                double score = r.getFinalScore();
                sumOfAllScores += score;
                countOfAllScores++;

                if (!hasAnyScore) {
                    highest = score;
                    lowest = score;
                    hasAnyScore = true;
                } else {
                    highest = Math.max(highest, score);
                    lowest = Math.min(lowest, score);
                }
>>>>>>> develop
            }
        }

        summary.setModalitiesBreakdown(breakdowns);
        summary.getTotals().setTotalModalities(modalities.size());
        summary.getTotals().setApprovedEnrollments(totalParticipants);

<<<<<<< HEAD
        summary.getEvaluationMetrics().setOverallAverage(
                countOfAllScores > 0 ? sumOfAllScores / countOfAllScores : 0.0);
        summary.getEvaluationMetrics().setHighestScore(
                countOfAllScores > 0 ? highest : 0.0);
        summary.getEvaluationMetrics().setLowestScore(
                countOfAllScores > 0 ? lowest : 0.0);

        summary.setGeneratedAt(Instant.now());
=======
        summary.getEvaluationMetrics().setOverallAverage(countOfAllScores > 0 ? sumOfAllScores / countOfAllScores : 0.0);
        summary.getEvaluationMetrics().setHighestScore(hasAnyScore ? highest : 0.0);
        summary.getEvaluationMetrics().setLowestScore(hasAnyScore ? lowest : 0.0);

        // 4. Obtener datos del cronograma desde ms-scheduling
        try {
            ScheduleResponseDto scheduleDto = schedulingClient.getScheduleByEvent(eventId);
            if (scheduleDto != null) {
                summary.getScheduleMetrics().setTotalSlots(scheduleDto.getTotalSlots() != null ? scheduleDto.getTotalSlots() : 0);
                summary.getScheduleMetrics().setScheduleStatus("CONFIGURED");
                summary.getScheduleMetrics().setGeneratedAt(scheduleDto.getGeneratedAt() != null ? scheduleDto.getGeneratedAt().toString() : Instant.now().toString());
            }
        } catch (Exception e) {
            log.warn("No se pudo obtener el cronograma del evento {} desde ms-scheduling: {}", eventId, e.getMessage());
            summary.getScheduleMetrics().setScheduleStatus("PENDING_OR_UNAVAILABLE");
        }

        summary.setGeneratedAt(Instant.now());
        summary.setStatus("CONSOLIDATED");
>>>>>>> develop

        return eventSummaryRepository.save(summary);
    }
}
