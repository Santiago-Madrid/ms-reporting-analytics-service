package com.wd.ms_reporting_analytics_service.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.wd.ms_reporting_analytics_service.domain.EventSummary;
import com.wd.ms_reporting_analytics_service.dto.EnrollmentReportFilter;
import com.wd.ms_reporting_analytics_service.dto.EnrollmentReportResponse;
import com.wd.ms_reporting_analytics_service.dto.EventSummaryReportResponse;
import com.wd.ms_reporting_analytics_service.security.ReportAccessGuard;
import com.wd.ms_reporting_analytics_service.service.EnrollmentReportService;
import com.wd.ms_reporting_analytics_service.service.ReportGenerationService;
import com.wd.ms_reporting_analytics_service.service.ReportNotices;

import lombok.RequiredArgsConstructor;

/**
 * Version JSON de los KPIs que hoy solo se ven dentro del PDF/Excel
 * general, para poder pintarlos en la pantalla de detalle de reportes.
 *
 * Los conteos de inscritos/pista musical salen de EnrollmentReportService
 * (datos reales de ms-enrollment/ms-music-media), no de EventSummary: ese
 * read model cuenta "participantes" a partir de resultados de jurado, asi
 * que para un evento sin calificar todavia mostraba 0 aunque hubiera
 * inscritos aprobados de verdad.
 */
@RestController
@RequestMapping("/reports/events/{eventId}/summary")
@RequiredArgsConstructor
public class EventSummaryReportController {

    private final ReportAccessGuard reportAccessGuard;
    private final ReportGenerationService reportGenerationService;
    private final EnrollmentReportService enrollmentReportService;

    @GetMapping
    public ResponseEntity<EventSummaryReportResponse> getSummary(
            @PathVariable Long eventId,
            @RequestHeader("X-User-Id") Long authenticatedUserId) {

        reportAccessGuard.assertOwnerOrEventAdmin(eventId, authenticatedUserId);

        EventSummary summary = reportGenerationService.buildReportData(eventId, authenticatedUserId).summary();
        EnrollmentReportResponse enrollments = enrollmentReportService.buildReport(eventId, EnrollmentReportFilter.ALL, authenticatedUserId);
        EnrollmentReportResponse.Counters counters = enrollments.getCounters();

        List<EventSummaryReportResponse.ModalityBreakdownDto> modalities = summary.getModalitiesBreakdown() == null
                ? List.of()
                : summary.getModalitiesBreakdown().stream()
                        .map(m -> EventSummaryReportResponse.ModalityBreakdownDto.builder()
                                .modalityId(m.getModalityId())
                                .category(m.getCategory())
                                .division(m.getDivision())
                                .participantCount(m.getParticipantCount())
                                .averageScore(m.getAverageScore())
                                .build())
                        .toList();

        EventSummaryReportResponse response = EventSummaryReportResponse.builder()
                .eventId(eventId)
                .eventName(summary.getEventName())
                .executionDate(summary.getExecutionDate())
                .totalParticipants(counters.getApproved())
                .totalEnrolled(counters.getTotal())
                .totalPending(counters.getPending())
                .totalRejected(counters.getRejected())
                .totalWithMusicTrack(counters.getWithTrack())
                .totalWithoutMusicTrack(counters.getWithoutTrack())
                .totalModalities(summary.getTotals().getTotalModalities())
                .overallAverageScore(summary.getEvaluationMetrics().getOverallAverage())
                .highestScore(summary.getEvaluationMetrics().getHighestScore())
                .lowestScore(summary.getEvaluationMetrics().getLowestScore())
                .totalScheduledSlots(summary.getScheduleMetrics() != null ? summary.getScheduleMetrics().getTotalSlots() : 0)
                .scheduleStatus(summary.getScheduleMetrics() != null ? summary.getScheduleMetrics().getScheduleStatus() : "N/A")
                .modalities(modalities)
                .participantsNotice(counters.getApproved() == 0
                        ? "Este evento todavía no tiene inscripciones aprobadas registradas." : null)
                .scoringNotice(ReportNotices.scoringNotice(summary))
                .scheduleNotice(ReportNotices.scheduleNotice(
                        summary.getScheduleMetrics() != null ? summary.getScheduleMetrics().getScheduleStatus() : null))
                .build();

        return ResponseEntity.ok(response);
    }
}
