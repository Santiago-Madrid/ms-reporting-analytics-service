package com.wd.ms_reporting_analytics_service.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.wd.ms_reporting_analytics_service.client.EventCategoryClient;
import com.wd.ms_reporting_analytics_service.client.EventClient;
import com.wd.ms_reporting_analytics_service.client.SchedulingReportClient;
import com.wd.ms_reporting_analytics_service.client.ScoringClient;
import com.wd.ms_reporting_analytics_service.domain.EventSummary;
import com.wd.ms_reporting_analytics_service.dto.external.EventResponseDto;
import com.wd.ms_reporting_analytics_service.dto.external.HttpGlobalResponse;
import com.wd.ms_reporting_analytics_service.dto.external.ModalityResponseDto;
import com.wd.ms_reporting_analytics_service.dto.external.ResultResponse;
import com.wd.ms_reporting_analytics_service.dto.external.ScheduleResponseDto;
import com.wd.ms_reporting_analytics_service.repository.EventSummaryRepository;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Orquesta la actualizacion del read model event_summaries con resiliencia.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class EventSummaryConsolidationService {

    private final EventCategoryClient eventCategoryClient;
    private final EventClient eventClient;
    private final ScoringClient scoringClient;
    private final SchedulingReportClient schedulingReportClient;
    private final EventSummaryRepository eventSummaryRepository;

    /** Sincroniza sin un usuario puntual en contexto (ej. InternalSyncController): usa el dueño del evento. */
    public EventSummary syncEventSummary(Long eventId) {
        return syncEventSummary(eventId, null);
    }

    public EventSummary syncEventSummary(Long eventId, Long requestingUserId) {
        String eventIdStr = String.valueOf(eventId);

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
            }
        }

        summary.setModalitiesBreakdown(breakdowns);
        summary.getTotals().setTotalModalities(modalities.size());
        summary.getTotals().setApprovedEnrollments(totalParticipants);

        summary.getEvaluationMetrics().setOverallAverage(countOfAllScores > 0 ? sumOfAllScores / countOfAllScores : 0.0);
        summary.getEvaluationMetrics().setHighestScore(hasAnyScore ? highest : 0.0);
        summary.getEvaluationMetrics().setLowestScore(hasAnyScore ? lowest : 0.0);

        // 4. Obtener datos del cronograma desde ms-scheduling.
        // ms-scheduling exige el header X-User-Id y, si el cronograma esta en
        // borrador, solo lo muestra al dueno del evento o a STAFF/JURY. Si no
        // hay un usuario puntual pidiendo el reporte (ej. sync interno), se usa
        // el dueno del evento (ya resuelto arriba) como solicitante razonable.
        Long scheduleRequesterId = requestingUserId != null ? requestingUserId : summary.getOwnerId();
        try {
            if (scheduleRequesterId == null) {
                throw new IllegalStateException("No hay un usuario ni un dueno de evento para consultar el cronograma.");
            }
            HttpGlobalResponse<ScheduleResponseDto> scheduleResp = schedulingReportClient.getScheduleByEvent(eventId, scheduleRequesterId);
            ScheduleResponseDto scheduleDto = scheduleResp != null ? scheduleResp.getData() : null;
            if (scheduleDto != null) {
                List<?> slots = scheduleDto.getSchedules();
                int totalSlots = scheduleDto.getTotalSlots() != null ? scheduleDto.getTotalSlots() : (slots != null ? slots.size() : 0);
                summary.getScheduleMetrics().setTotalSlots(totalSlots);
                summary.getScheduleMetrics().setScheduleStatus("CONFIGURED");
                summary.getScheduleMetrics().setGeneratedAt(scheduleDto.getGeneratedAt() != null ? scheduleDto.getGeneratedAt().toString() : Instant.now().toString());
            } else {
                summary.getScheduleMetrics().setScheduleStatus("NOT_CONFIGURED");
            }
        } catch (FeignException.NotFound e) {
            summary.getScheduleMetrics().setScheduleStatus("NOT_CONFIGURED");
        } catch (FeignException.BadRequest e) {
            summary.getScheduleMetrics().setScheduleStatus("DRAFT_NOT_VISIBLE");
        } catch (Exception e) {
            log.warn("No se pudo obtener el cronograma del evento {} desde ms-scheduling: {}", eventId, e.getMessage());
            summary.getScheduleMetrics().setScheduleStatus("PENDING_OR_UNAVAILABLE");
        }

        summary.setGeneratedAt(Instant.now());
        summary.setStatus("CONSOLIDATED");

        return eventSummaryRepository.save(summary);
    }
}
