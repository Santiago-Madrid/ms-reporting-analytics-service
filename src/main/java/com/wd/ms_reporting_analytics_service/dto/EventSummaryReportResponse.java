package com.wd.ms_reporting_analytics_service.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * KPIs de un evento en formato JSON, para pintarlos en la pantalla de
 * detalle de reportes del frontend sin tener que parsear el PDF/Excel.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventSummaryReportResponse {
    private Long eventId;
    private String eventName;
    private String executionDate;

    /** Real: inscripciones con status APPROVED (via ms-enrollment), no resultados de jurado. */
    private int totalParticipants;

    private int totalEnrolled;
    private int totalPending;
    private int totalRejected;
    private int totalWithMusicTrack;
    private int totalWithoutMusicTrack;

    private int totalModalities;
    private double overallAverageScore;
    private double highestScore;
    private double lowestScore;
    private Integer totalScheduledSlots;
    private String scheduleStatus;
    private List<ModalityBreakdownDto> modalities;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ModalityBreakdownDto {
        private Long modalityId;
        private String category;
        private String division;
        private Integer participantCount;
        private Double averageScore;
    }
}
