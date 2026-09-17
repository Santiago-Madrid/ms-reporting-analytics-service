package com.wd.ms_reporting_analytics_service.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;

import com.wd.ms_reporting_analytics_service.client.EventClient;
import com.wd.ms_reporting_analytics_service.client.SchedulingReportClient;
import com.wd.ms_reporting_analytics_service.dto.ScheduleReportResponse;
import com.wd.ms_reporting_analytics_service.dto.external.EventResponseDto;
import com.wd.ms_reporting_analytics_service.dto.external.HttpGlobalResponse;
import com.wd.ms_reporting_analytics_service.dto.external.ScheduleResponseDto;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Arma el reporte de cronograma de un evento. "Generado" = existe una fila
 * Schedule para el evento en ms-scheduling (GET .../event/{eventId} no
 * responde 404). Si el cronograma esta en borrador y quien lo pide no es
 * el organizador ni tiene rol STAFF/JURY en el evento, ms-scheduling
 * responde 400 (BadRequestException) en vez de datos: se traduce a
 * DRAFT_NOT_VISIBLE en lugar de reventar como error.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ScheduleReportService {

    private final SchedulingReportClient schedulingReportClient;
    private final EventClient eventClient;

    public ScheduleReportResponse getScheduleReport(Long eventId, Long requestingUserId) {
        String eventName = resolveEventName(eventId);

        try {
            HttpGlobalResponse<ScheduleResponseDto> response = schedulingReportClient.getScheduleByEvent(eventId, requestingUserId);
            ScheduleResponseDto dto = response != null ? response.getData() : null;

            if (dto == null) {
                return unavailable(eventId, eventName, "NOT_CONFIGURED");
            }

            List<ScheduleResponseDto.ScheduleSlotDto> slots = dto.getSchedules() != null ? dto.getSchedules() : List.of();
            List<ScheduleResponseDto.ScheduleSlotDto> sorted = slots.stream()
                    .sorted(Comparator.comparing(s -> s.getOrder() == null ? Integer.MAX_VALUE : s.getOrder()))
                    .toList();

            return ScheduleReportResponse.builder()
                    .eventId(eventId)
                    .eventName(eventName)
                    .available(true)
                    .status("AVAILABLE")
                    .docId(buildDocId(eventId))
                    .totalSlots(dto.getTotalSlots() != null ? dto.getTotalSlots() : sorted.size())
                    .generatedAt(dto.getGeneratedAt())
                    .slots(sorted)
                    .build();
        } catch (FeignException.NotFound e) {
            return unavailable(eventId, eventName, "NOT_CONFIGURED");
        } catch (FeignException.BadRequest e) {
            return unavailable(eventId, eventName, "DRAFT_NOT_VISIBLE");
        } catch (Exception e) {
            log.warn("No se pudo obtener el cronograma del evento {}: {}", eventId, e.getMessage());
            return unavailable(eventId, eventName, "NOT_CONFIGURED");
        }
    }

    private static final DateTimeFormatter DATETIME_DISPLAY = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter TIME_DISPLAY = DateTimeFormatter.ofPattern("dd/MM HH:mm");

    /** Fila lista para la plantilla: fechas ya formateadas, sin ternarios en el HTML. */
    public record SlotView(Integer order, String participantOrGroup, String modality, String style,
                            String stage, String startTime, String endTime, String status) { }

    /** Contexto listo para pasarle a la plantilla Thymeleaf "schedule". */
    public Context toTemplateContext(ScheduleReportResponse data) {
        Context context = new Context();
        context.setVariable("eventName", data.getEventName());
        context.setVariable("docId", data.getDocId());
        context.setVariable("totalSlots", data.getTotalSlots());
        context.setVariable("generatedAt", data.getGeneratedAt() != null ? DATETIME_DISPLAY.format(data.getGeneratedAt()) : "N/A");

        List<SlotView> views = data.getSlots() == null ? List.of() : data.getSlots().stream()
                .map(s -> new SlotView(
                        s.getOrder(),
                        s.getParticipantName() != null && !s.getParticipantName().isBlank() ? s.getParticipantName() : s.getGroupName(),
                        (s.getCategory() == null ? "" : s.getCategory()) + " - " + (s.getDivision() == null ? "" : s.getDivision()),
                        s.getStyle(),
                        s.getStage(),
                        s.getStartTime() != null ? TIME_DISPLAY.format(s.getStartTime()) : "-",
                        s.getEndTime() != null ? TIME_DISPLAY.format(s.getEndTime()) : "-",
                        s.getStatus()))
                .toList();
        context.setVariable("slots", views);
        return context;
    }

    private String buildDocId(Long eventId) {
        return "WD-CRN-" + eventId + "-" + DateTimeFormatter.ofPattern("yyyyMMdd").format(LocalDate.now());
    }

    private ScheduleReportResponse unavailable(Long eventId, String eventName, String status) {
        return ScheduleReportResponse.builder()
                .eventId(eventId)
                .eventName(eventName)
                .available(false)
                .status(status)
                .totalSlots(0)
                .slots(List.of())
                .build();
    }

    private String resolveEventName(Long eventId) {
        try {
            HttpGlobalResponse<EventResponseDto> response = eventClient.getEventById(eventId);
            if (response != null && response.getData() != null && response.getData().getName() != null) {
                return response.getData().getName();
            }
        } catch (Exception e) {
            log.warn("No se pudo obtener el nombre del evento {}: {}", eventId, e.getMessage());
        }
        return "Evento #" + eventId;
    }
}
