package com.wd.ms_reporting_analytics_service.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.wd.ms_reporting_analytics_service.service.ExcelGeneratorService;
import com.wd.ms_reporting_analytics_service.service.ReportGenerationService;

import lombok.RequiredArgsConstructor;

/** RF-62: export a Excel, en ruta separada de PDF para mejor control (segun definiste). */
@RestController
@RequestMapping("/reports/events/{eventId}/export/excel")
@RequiredArgsConstructor
public class ReportExcelExportController {

    private final ReportGenerationService reportGenerationService;
    private final ExcelGeneratorService excelGeneratorService;

    @GetMapping(produces = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    public ResponseEntity<byte[]> exportExcel(@PathVariable Long eventId) {
        ReportGenerationService.ReportData data = reportGenerationService.buildReportData(eventId);
        byte[] excelBytes = excelGeneratorService.generateExcel(data.summary(), data.narrative());

        return ResponseEntity.ok()
                .contentType(org.springframework.http.MediaType
                        .parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=reporte-evento-" + eventId + ".xlsx")
                .body(excelBytes);
    }
}
