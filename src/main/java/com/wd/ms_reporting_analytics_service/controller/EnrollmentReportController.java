package com.wd.ms_reporting_analytics_service.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.thymeleaf.context.Context;

import com.wd.ms_reporting_analytics_service.dto.EnrollmentReportFilter;
import com.wd.ms_reporting_analytics_service.dto.EnrollmentReportResponse;
import com.wd.ms_reporting_analytics_service.security.ReportAccessGuard;
import com.wd.ms_reporting_analytics_service.service.EnrollmentReportService;
import com.wd.ms_reporting_analytics_service.service.PdfGeneratorService;

import lombok.RequiredArgsConstructor;

/**
 * Reporte de inscritos de un evento: listado general, aprobados, no
 * aprobados, con pista musical publicada y con pista musical pendiente.
 */
@RestController
@RequestMapping("/reports/events/{eventId}/enrollments")
@RequiredArgsConstructor
public class EnrollmentReportController {

    private final ReportAccessGuard reportAccessGuard;
    private final EnrollmentReportService enrollmentReportService;
    private final PdfGeneratorService pdfGeneratorService;

    @GetMapping
    public ResponseEntity<EnrollmentReportResponse> getEnrollments(
            @PathVariable Long eventId,
            @RequestParam(name = "filter", defaultValue = "ALL") EnrollmentReportFilter filter,
            @RequestHeader("X-User-Id") Long authenticatedUserId) {

        reportAccessGuard.assertOwnerOrEventAdmin(eventId, authenticatedUserId);
        return ResponseEntity.ok(enrollmentReportService.buildReport(eventId, filter, authenticatedUserId));
    }

    @GetMapping(value = "/export/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> exportPdf(
            @PathVariable Long eventId,
            @RequestParam(name = "filter", defaultValue = "ALL") EnrollmentReportFilter filter,
            @RequestHeader("X-User-Id") Long authenticatedUserId) {

        reportAccessGuard.assertOwnerOrEventAdmin(eventId, authenticatedUserId);
        EnrollmentReportResponse data = enrollmentReportService.buildReport(eventId, filter, authenticatedUserId);
        Context context = enrollmentReportService.toTemplateContext(data);
        byte[] pdfBytes = pdfGeneratorService.generatePdf("enrollment-list", context);

        String filename = "inscritos-evento-" + eventId + "-" + filter.name().toLowerCase() + ".pdf";
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .body(pdfBytes);
    }
}
