package com.wd.ms_reporting_analytics_service.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.wd.ms_reporting_analytics_service.dto.external.ScheduleResponseDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Respuesta del reporte de cronograma. "available" le dice al frontend si
 * corresponde mostrar el boton de descarga en PDF; "status" distingue por
 * que no esta disponible cuando no lo esta.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleReportResponse {
    private Long eventId;
    private String eventName;
    private boolean available;

    /** NOT_CONFIGURED | DRAFT_NOT_VISIBLE | AVAILABLE */
    private String status;

    private String docId;
    private Integer totalSlots;
    private LocalDateTime generatedAt;
    private List<ScheduleResponseDto.ScheduleSlotDto> slots;
}
