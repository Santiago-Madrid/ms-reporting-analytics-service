package com.wd.ms_reporting_analytics_service.dto;

import java.time.Instant;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnrollmentReportResponse {
    private Long eventId;
    private String eventName;
    private String filter;
    private String reportTitle;
    private String docId;
    private Instant generatedAt;
    private Counters counters;
    private List<EnrollmentReportRow> rows;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Counters {
        private int total;
        private int approved;
        private int pending;
        private int rejected;
        private int withTrack;
        private int withoutTrack;
    }
}
