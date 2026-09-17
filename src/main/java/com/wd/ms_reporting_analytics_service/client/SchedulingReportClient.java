package com.wd.ms_reporting_analytics_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

import com.wd.ms_reporting_analytics_service.dto.external.HttpGlobalResponse;
import com.wd.ms_reporting_analytics_service.dto.external.ScheduleResponseDto;

/**
 * Cliente Feign hacia ms-scheduling, usado solo por el nuevo reporte de
 * "PDF del cronograma". Es un cliente separado del SchedulingClient
 * existente (que sigue usando EventSummaryConsolidationService sin
 * cambios): el controller real de ms-scheduling exige el header
 * X-User-Id y devuelve el payload envuelto en HttpGlobalResponse, algo
 * que el SchedulingClient original no declara correctamente. En vez de
 * arriesgar el flujo existente editandolo, este cliente nuevo lo declara
 * bien desde el principio.
 *
 * Devuelve 404 (mapeado a "cronograma no generado") si no existe un
 * Schedule para el evento, segun SchedulingService.getScheduleByEvent.
 */
@FeignClient(name = "ms-scheduling", contextId = "schedulingReportClient", path = "/api/v1/scheduling")
public interface SchedulingReportClient {

    @GetMapping("/event/{eventId}")
    HttpGlobalResponse<ScheduleResponseDto> getScheduleByEvent(
            @PathVariable("eventId") Long eventId,
            @RequestHeader("X-User-Id") Long userId);
}
