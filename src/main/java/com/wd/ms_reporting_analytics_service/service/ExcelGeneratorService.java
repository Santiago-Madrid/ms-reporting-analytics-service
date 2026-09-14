package com.wd.ms_reporting_analytics_service.service;

import java.io.ByteArrayOutputStream;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import com.wd.ms_reporting_analytics_service.domain.EventSummary;
import com.wd.ms_reporting_analytics_service.dto.AiReportResponse;

/**
 * RF-62: export a Excel con los mismos datos que el PDF, tal como
 *  ("si lo mismo pero en su hoja de excel").
 */
@Service
public class ExcelGeneratorService {

    public byte[] generateExcel(EventSummary summary, AiReportResponse narrative) {
        try (XSSFWorkbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            CellStyle headerStyle = workbook.createCellStyle();
            Font boldFont = workbook.createFont();
            boldFont.setBold(true);
            headerStyle.setFont(boldFont);

            buildResumenSheet(workbook, headerStyle, summary);
            buildModalidadesSheet(workbook, headerStyle, summary.getModalitiesBreakdown());
            buildNarrativaSheet(workbook, headerStyle, narrative);

            workbook.write(outputStream);
            return outputStream.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Error generando el Excel del reporte", e);
        }
    }

    private void buildResumenSheet(XSSFWorkbook workbook, CellStyle headerStyle, EventSummary summary) {
        Sheet sheet = workbook.createSheet("Resumen");

        Row header = sheet.createRow(0);
        String[] headers = {"Metrica", "Valor"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = header.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        Object[][] rows = {
                {"Evento", summary.getEventName()},
                {"Total participantes", summary.getTotals().getApprovedEnrollments()},
                {"Total modalidades", summary.getTotals().getTotalModalities()},
                {"Promedio general", summary.getEvaluationMetrics().getOverallAverage()},
                {"Puntaje mas alto", summary.getEvaluationMetrics().getHighestScore()},
                {"Puntaje mas bajo", summary.getEvaluationMetrics().getLowestScore()}
        };

        for (int i = 0; i < rows.length; i++) {
            Row row = sheet.createRow(i + 1);
            row.createCell(0).setCellValue(String.valueOf(rows[i][0]));
            Cell valueCell = row.createCell(1);
            Object value = rows[i][1];
            if (value instanceof Number number) {
                valueCell.setCellValue(number.doubleValue());
            } else {
                valueCell.setCellValue(String.valueOf(value));
            }
        }

        sheet.autoSizeColumn(0);
        sheet.autoSizeColumn(1);
    }

    private void buildModalidadesSheet(XSSFWorkbook workbook, CellStyle headerStyle,
                                        List<EventSummary.ModalityBreakdown> modalities) {
        Sheet sheet = workbook.createSheet("Modalidades");

        Row header = sheet.createRow(0);
        String[] headers = {"Categoria", "Division", "Participantes", "Promedio"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = header.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        if (modalities == null) return;

        int rowIndex = 1;
        for (EventSummary.ModalityBreakdown modality : modalities) {
            Row row = sheet.createRow(rowIndex++);
            row.createCell(0).setCellValue(modality.getCategory());
            row.createCell(1).setCellValue(modality.getDivision());
            row.createCell(2).setCellValue(modality.getParticipantCount());
            row.createCell(3).setCellValue(modality.getAverageScore());
        }

        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private void buildNarrativaSheet(XSSFWorkbook workbook, CellStyle headerStyle, AiReportResponse narrative) {
        Sheet sheet = workbook.createSheet("Narrativa IA");

        String[][] sections = {
                {"Introduccion", narrative.getIntroduccion()},
                {"Analisis por modalidad", narrative.getAnalisisPorModalidad()},
                {"Conclusion", narrative.getConclusion()}
        };

        int rowIndex = 0;
        for (String[] section : sections) {
            Row titleRow = sheet.createRow(rowIndex++);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue(section[0]);
            titleCell.setCellStyle(headerStyle);

            Row contentRow = sheet.createRow(rowIndex++);
            contentRow.createCell(0).setCellValue(section[1]);
            rowIndex++; // fila en blanco entre secciones
        }

        sheet.setColumnWidth(0, 20000);
    }
}
