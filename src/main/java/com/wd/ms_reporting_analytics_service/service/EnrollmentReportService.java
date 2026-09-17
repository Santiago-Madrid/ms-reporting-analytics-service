package com.wd.ms_reporting_analytics_service.service;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;

import com.wd.ms_reporting_analytics_service.client.EnrollmentClient;
import com.wd.ms_reporting_analytics_service.client.EventCategoryClient;
import com.wd.ms_reporting_analytics_service.client.EventClient;
import com.wd.ms_reporting_analytics_service.client.MusicMediaClient;
import com.wd.ms_reporting_analytics_service.dto.EnrollmentReportFilter;
import com.wd.ms_reporting_analytics_service.dto.EnrollmentReportResponse;
import com.wd.ms_reporting_analytics_service.dto.EnrollmentReportRow;
import com.wd.ms_reporting_analytics_service.dto.external.EnrollmentReportDto;
import com.wd.ms_reporting_analytics_service.dto.external.EventResponseDto;
import com.wd.ms_reporting_analytics_service.dto.external.HttpGlobalResponse;
import com.wd.ms_reporting_analytics_service.dto.external.ModalityResponseDto;
import com.wd.ms_reporting_analytics_service.dto.external.MusicTrackDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Arma el reporte de inscritos (listado / aprobados / no aprobados /
 * con-sin pista musical) cruzando datos reales de ms-enrollment,
 * ms-event-category (para el nombre de la modalidad) y ms-music-media
 * (para saber si ya subieron su pista). Es tolerante a fallas de datos
 * (como el resto de este servicio) porque la autorizacion ya la resolvio
 * ReportAccessGuard antes de llegar aqui.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EnrollmentReportService {

    private final EventClient eventClient;
    private final EventCategoryClient eventCategoryClient;
    private final EnrollmentClient enrollmentClient;
    private final MusicMediaClient musicMediaClient;

    public EnrollmentReportResponse buildReport(Long eventId, EnrollmentReportFilter filter, Long requestingUserId) {
        String eventName = resolveEventName(eventId);
        Map<Long, String> modalityNames = resolveModalityNames(eventId);
        List<EnrollmentReportDto> enrollments = safeGetEnrollments(eventId);

        List<EnrollmentReportRow> allRows = new ArrayList<>();
        int approved = 0, pending = 0, rejected = 0, withTrack = 0, withoutTrack = 0;

        for (EnrollmentReportDto e : enrollments) {
            String status = e.getStatus() == null ? "" : e.getStatus().toUpperCase();
            boolean isApproved = "APPROVED".equals(status);

            Boolean hasTrack = null;
            if (isApproved) {
                hasTrack = resolveHasTrack(e.getEnrollmentId(), requestingUserId);
                if (Boolean.TRUE.equals(hasTrack)) withTrack++; else withoutTrack++;
            }

            switch (status) {
                case "APPROVED" -> approved++;
                case "PENDING" -> pending++;
                case "REJECTED" -> rejected++;
                default -> { }
            }

            allRows.add(toRow(e, modalityNames, hasTrack));
        }

        List<EnrollmentReportRow> filteredRows = applyFilter(allRows, filter);

        EnrollmentReportResponse.Counters counters = EnrollmentReportResponse.Counters.builder()
                .total(enrollments.size())
                .approved(approved)
                .pending(pending)
                .rejected(rejected)
                .withTrack(withTrack)
                .withoutTrack(withoutTrack)
                .build();

        return EnrollmentReportResponse.builder()
                .eventId(eventId)
                .eventName(eventName)
                .filter(filter.name())
                .reportTitle(titleFor(filter))
                .docId(buildDocId(eventId))
                .generatedAt(Instant.now())
                .counters(counters)
                .rows(filteredRows)
                .build();
    }

    private String buildDocId(Long eventId) {
        return "WD-INS-" + eventId + "-" + DateTimeFormatter.ofPattern("yyyyMMdd").format(java.time.LocalDate.now());
    }

    private String formatGeneratedAt(Instant instant) {
        if (instant == null) return "N/A";
        return DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
                .withZone(java.time.ZoneId.systemDefault())
                .format(instant);
    }

    /** Conteo real de inscritos (sin consultar pistas musicales), para KPIs livianos. */
    public record EnrollmentCounts(int total, int approved, int pending, int rejected) { }

    public EnrollmentCounts countEnrollments(Long eventId) {
        List<EnrollmentReportDto> enrollments = safeGetEnrollments(eventId);
        int approved = 0, pending = 0, rejected = 0;
        for (EnrollmentReportDto e : enrollments) {
            String status = e.getStatus() == null ? "" : e.getStatus().toUpperCase();
            switch (status) {
                case "APPROVED" -> approved++;
                case "PENDING" -> pending++;
                case "REJECTED" -> rejected++;
                default -> { }
            }
        }
        return new EnrollmentCounts(enrollments.size(), approved, pending, rejected);
    }

    /** Contexto listo para pasarle a la plantilla Thymeleaf "enrollment-list". */
    public Context toTemplateContext(EnrollmentReportResponse data) {
        Context context = new Context();
        context.setVariable("eventName", data.getEventName());
        context.setVariable("reportTitle", data.getReportTitle());
        context.setVariable("docId", data.getDocId());
        context.setVariable("generatedAt", formatGeneratedAt(data.getGeneratedAt()));
        context.setVariable("counters", data.getCounters());
        context.setVariable("rows", data.getRows());
        return context;
    }

    private EnrollmentReportRow toRow(EnrollmentReportDto e, Map<Long, String> modalityNames, Boolean hasTrack) {
        EnrollmentReportDto.ParticipantDto p = e.getParticipant();
        String fullName = p != null
                ? (nullToEmpty(p.getName()) + " " + nullToEmpty(p.getLastName())).trim()
                : "—";
        fullName = fullName.isEmpty() ? "—" : fullName;

        return EnrollmentReportRow.builder()
                .enrollmentId(e.getEnrollmentId())
                .fullName(fullName)
                .initials(initialsOf(fullName))
                .documentNumber(p != null ? p.getDocumentNumber() : null)
                .email(p != null ? p.getEmail() : null)
                .modalityName(modalityNames.getOrDefault(e.getModalityId(), "—"))
                .status(e.getStatus())
                .statusLabel(statusLabel(e.getStatus()))
                .statusCssClass(statusCssClass(e.getStatus()))
                .hasMusicTrack(hasTrack)
                .trackLabel(trackLabel(hasTrack))
                .trackCssClass(trackCssClass(hasTrack))
                .createdAt(e.getCreatedAt())
                .build();
    }

    private List<EnrollmentReportRow> applyFilter(List<EnrollmentReportRow> rows, EnrollmentReportFilter filter) {
        return switch (filter) {
            case ALL -> rows;
            case APPROVED -> rows.stream().filter(r -> "APPROVED".equals(r.getStatus())).toList();
            case NOT_APPROVED -> rows.stream().filter(r -> !"APPROVED".equals(r.getStatus())).toList();
            case WITH_TRACK -> rows.stream().filter(r -> Boolean.TRUE.equals(r.getHasMusicTrack())).toList();
            case WITHOUT_TRACK -> rows.stream().filter(r -> Boolean.FALSE.equals(r.getHasMusicTrack())).toList();
        };
    }

    private String titleFor(EnrollmentReportFilter filter) {
        return switch (filter) {
            case ALL -> "Listado de Inscritos";
            case APPROVED -> "Inscritos Aprobados";
            case NOT_APPROVED -> "Inscritos No Aprobados";
            case WITH_TRACK -> "Inscritos con Pista Musical Publicada";
            case WITHOUT_TRACK -> "Inscritos con Pista Musical Pendiente";
        };
    }

    private String statusLabel(String status) {
        if (status == null) return "—";
        return switch (status.toUpperCase()) {
            case "APPROVED" -> "Aprobado";
            case "PENDING" -> "Pendiente";
            case "REJECTED" -> "Rechazado";
            default -> status;
        };
    }

    private String statusCssClass(String status) {
        if (status == null) return "";
        return switch (status.toUpperCase()) {
            case "APPROVED" -> "status-approved";
            case "PENDING" -> "status-pending";
            case "REJECTED" -> "status-rejected";
            default -> "";
        };
    }

    private String trackLabel(Boolean hasTrack) {
        if (hasTrack == null) return "—";
        return hasTrack ? "Publicada" : "Pendiente";
    }

    private String trackCssClass(Boolean hasTrack) {
        if (hasTrack == null) return "";
        return hasTrack ? "status-track-yes" : "status-track-no";
    }

    private String nullToEmpty(String s) {
        return s == null ? "" : s;
    }

    private String initialsOf(String fullName) {
        if (fullName == null || fullName.isBlank() || "—".equals(fullName)) return "?";
        String[] parts = fullName.trim().split("\\s+");
        String first = String.valueOf(parts[0].charAt(0));
        String last = parts.length > 1 ? String.valueOf(parts[parts.length - 1].charAt(0)) : "";
        return (first + last).toUpperCase();
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

    private Map<Long, String> resolveModalityNames(Long eventId) {
        try {
            HttpGlobalResponse<List<ModalityResponseDto>> response = eventCategoryClient.getModalitiesByEventId(eventId);
            List<ModalityResponseDto> modalities = response != null && response.getData() != null ? response.getData() : List.of();
            return modalities.stream()
                    .filter(m -> m.getId() != null)
                    .collect(Collectors.toMap(ModalityResponseDto::getId,
                            m -> nullToEmpty(m.getCategory()) + " - " + nullToEmpty(m.getDivision())));
        } catch (Exception e) {
            log.warn("No se pudieron obtener las modalidades del evento {}: {}", eventId, e.getMessage());
            return Map.of();
        }
    }

    private List<EnrollmentReportDto> safeGetEnrollments(Long eventId) {
        try {
            List<EnrollmentReportDto> list = enrollmentClient.getEnrollmentsByEvent(eventId);
            return list != null ? list : List.of();
        } catch (Exception e) {
            log.warn("No se pudieron obtener los inscritos del evento {}: {}", eventId, e.getMessage());
            return List.of();
        }
    }

    private Boolean resolveHasTrack(Long enrollmentId, Long requestingUserId) {
        try {
            HttpGlobalResponse<MusicTrackDto> response = musicMediaClient.getMetadata(enrollmentId, requestingUserId);
            MusicTrackDto track = response != null ? response.getData() : null;
            return track != null && Boolean.TRUE.equals(track.getIsActive());
        } catch (Exception e) {
            // ms-music-media responde error (400) cuando la inscripcion todavia no tiene
            // pista subida: es el resultado esperado, no un fallo que deba registrarse.
            return false;
        }
    }
}
