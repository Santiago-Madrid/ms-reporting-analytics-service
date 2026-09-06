package com.wd.ms_reporting_analytics_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.wd.ms_reporting_analytics_service.dto.external.ScheduleResponseDto;

/**
 * Cliente Feign hacia ms-scheduling.
 *
 * name = "ms-scheduling" -> coincide con spring.application.name en ms-scheduling.
 * path = "/api/v1/scheduling" -> context-path (/api/v1) + controller mapping (/scheduling).
 */
@FeignClient(name = "ms-scheduling", path = "/api/v1/scheduling")
public interface SchedulingClient {

    @GetMapping("/event/{eventId}")
    ScheduleResponseDto getScheduleByEvent(@PathVariable("eventId") Long eventId);
}
