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

import com.wd.ms_reporting_analytics_service.security.ReportAccessGuard;
import com.wd.ms_reporting_analytics_service.service.PdfGeneratorService;
import com.wd.ms_reporting_analytics_service.service.ReportGenerationService;

import lombok.RequiredArgsConstructor;

/** RF-62: export a PDF, en ruta separada de Excel para mejor control (segun definiste). */
@RestController
@RequestMapping("/reports/events/{eventId}/export/pdf")
@RequiredArgsConstructor
public class ReportPdfExportController {

    private final ReportGenerationService reportGenerationService;
    private final PdfGeneratorService pdfGeneratorService;
    private final ReportAccessGuard reportAccessGuard;

    @GetMapping(produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> exportPdf(@PathVariable Long eventId, @RequestHeader("X-User-Id") Long authenticatedUserId) {
        reportAccessGuard.assertOwnerOrEventAdmin(eventId, authenticatedUserId);
        ReportGenerationService.ReportData data = reportGenerationService.buildReportData(eventId, authenticatedUserId);
        Context context = reportGenerationService.toTemplateContext(data);
        byte[] pdfBytes = pdfGeneratorService.generatePdf("report", context);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=reporte-evento-" + eventId + ".pdf")
                .body(pdfBytes);
    }
}
