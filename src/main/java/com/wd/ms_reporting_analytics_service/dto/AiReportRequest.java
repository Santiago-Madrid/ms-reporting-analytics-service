package com.wd.ms_reporting_analytics_service.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Payload que ms-reports envia al MCP de Python.
 * Va con datos ya numericos/consolidados (no manda ids tecnicos internos,
 * solo lo que la IA necesita para redactar el texto).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AiReportRequest {
    private String eventName;
    private int totalParticipants;
    private int totalModalities;
    private double overallAverageScore;
    private double highestScore;
    private double lowestScore;
    private List<ModalityStat> modalities;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ModalityStat {
        private String category;
        private String division;
        private int participantCount;
        private double averageScore;
    }
}