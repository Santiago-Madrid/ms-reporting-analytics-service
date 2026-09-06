package com.wd.ms_reporting_analytics_service.dto.external;

import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleResponseDto {
    private Long eventId;
    private String eventName;
    private LocalDateTime eventStartDate;
    private LocalDateTime eventEndDate;
    private Integer totalSlots;
    private LocalDateTime generatedAt;
    private List<ScheduleSlotDto> schedules;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ScheduleSlotDto {
        private Long id;
        private Long enrollmentId;
        private String participantName;
        private String groupName;
        private String division;
        private String category;
        private String style;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private String stage;
        private Integer order;
        private String status;
        private String notes;
    }
}
