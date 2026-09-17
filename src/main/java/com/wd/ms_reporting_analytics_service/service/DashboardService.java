package com.wd.ms_reporting_analytics_service.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.wd.ms_reporting_analytics_service.domain.EventSummary;
import com.wd.ms_reporting_analytics_service.dto.DashboardSummaryResponse;
import com.wd.ms_reporting_analytics_service.repository.EventSummaryRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * RF-55: Panel de resumen - agrega los event_summaries del propio usuario.
 *
 * No existe un rol "Administrador" global en la plataforma (el rol ADMIN
 * se verifica por evento, via user_event_roles de ms-enrollment), asi que
 * este resumen se acota a "mis eventos" (ownerId == X-User-Id) en vez de
 * agregar todos los eventos del sistema: asi cada usuario solo ve sus
 * propios datos, sin necesidad de inventar un rol de plataforma.
 *
 * "registeredUsers" y "approvedEnrollments" YA NO salen de EventSummary
 * (ese read model nunca setea registeredUsers, y approvedEnrollments en
 * realidad cuenta resultados de jurado, no inscripciones reales -- lo
 * dejaba en 0 para eventos sin calificar todavia). Se calculan en vivo
 * contra ms-enrollment (via EnrollmentReportService, mismo cliente Feign
 * que ya usa el reporte de inscritos) para que el panel muestre numeros
 * reales. El resto de metricas (modalidades, promedio de puntajes) si
 * son correctas en el read model y se mantienen igual.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardService {

    private final EventSummaryRepository eventSummaryRepository;
    private final EnrollmentReportService enrollmentReportService;

    public DashboardSummaryResponse getSummary(Long ownerId) {
        List<EventSummary> allSummaries = eventSummaryRepository.findByOwnerId(ownerId);

        long totalEvents = allSummaries.size();
        int totalModalities = allSummaries.stream()
                .mapToInt(s -> s.getTotals().getTotalModalities())
                .sum();
        double totalRevenue = allSummaries.stream()
                .mapToDouble(s -> s.getTotals().getTotalRevenue())
                .sum();
        double overallAverageScore = allSummaries.stream()
                .mapToDouble(s -> s.getEvaluationMetrics().getOverallAverage())
                .average()
                .orElse(0.0);

        int totalRegisteredUsers = 0;
        int totalApprovedEnrollments = 0;
        for (EventSummary summary : allSummaries) {
            Long eventId = parseEventId(summary.getEventId());
            if (eventId == null) continue;

            EnrollmentReportService.EnrollmentCounts counts = enrollmentReportService.countEnrollments(eventId);
            totalRegisteredUsers += counts.total();
            totalApprovedEnrollments += counts.approved();
        }

        return new DashboardSummaryResponse(
                totalEvents,
                totalRegisteredUsers,
                totalApprovedEnrollments,
                totalModalities,
                totalRevenue,
                overallAverageScore);
    }

    private Long parseEventId(String eventId) {
        try {
            return eventId != null ? Long.valueOf(eventId) : null;
        } catch (NumberFormatException e) {
            log.warn("eventId no numerico en event_summaries: {}", eventId);
            return null;
        }
    }
}
