package com.wd.ms_reporting_analytics_service.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.thymeleaf.context.Context;

import com.wd.ms_reporting_analytics_service.dto.ScheduleReportResponse;
import com.wd.ms_reporting_analytics_service.security.ReportAccessGuard;
import com.wd.ms_reporting_analytics_service.service.PdfGeneratorService;
import com.wd.ms_reporting_analytics_service.service.ScheduleReportService;

import lombok.RequiredArgsConstructor;

/**
 * Reporte de cronograma de un evento: JSON con disponibilidad (para que el
 * frontend decida si mostrar el boton de descarga) y PDF con los turnos.
 */
@RestController
@RequestMapping("/reports/events/{eventId}/schedule")
@RequiredArgsConstructor
public class ScheduleReportController {

    private final ReportAccessGuard reportAccessGuard;
    private final ScheduleReportService scheduleReportService;
    private final PdfGeneratorService pdfGeneratorService;

    @GetMapping
    public ResponseEntity<ScheduleReportResponse> getSchedule(
            @PathVariable Long eventId,
            @RequestHeader("X-User-Id") Long authenticatedUserId) {

        reportAccessGuard.assertOwnerOrEventAdmin(eventId, authenticatedUserId);
        return ResponseEntity.ok(scheduleReportService.getScheduleReport(eventId, authenticatedUserId));
    }

    @GetMapping(value = "/export/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> exportPdf(
            @PathVariable Long eventId,
            @RequestHeader("X-User-Id") Long authenticatedUserId) {

        reportAccessGuard.assertOwnerOrEventAdmin(eventId, authenticatedUserId);
        ScheduleReportResponse data = scheduleReportService.getScheduleReport(eventId, authenticatedUserId);

        // Aunque no haya cronograma generado (o este en borrador), se genera igual
        // el PDF con un aviso explicando por que, en vez de responder un error crudo:
        // quien pide el reporte tambien quiere un documento descargable como constancia.
        Context context = scheduleReportService.toTemplateContext(data);
        byte[] pdfBytes = pdfGeneratorService.generatePdf("schedule", context);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=cronograma-evento-" + eventId + ".pdf")
                .body(pdfBytes);
    }
}
