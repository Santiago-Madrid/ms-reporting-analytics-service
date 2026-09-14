package com.wd.ms_reporting_analytics_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.wd.ms_reporting_analytics_service.dto.external.EventResponseDto;
import com.wd.ms_reporting_analytics_service.dto.external.HttpGlobalResponse;

/**
 * Cliente Feign hacia ms-event-category, para traer nombre/fecha del
 * evento (y ownerId, util para mas adelante si se valida que el
 * organizador que pide el reporte sea el dueno del evento).
 *
 * name = "ms-event-category" -> mismo servicio que EventCategoryClient,
 * confirmado por su application.yml.
 *
 * path = "/api/v1/events" -> context-path (/api/v1) + @RequestMapping
 * base del EventController (/events).
 */
<<<<<<< HEAD
@FeignClient(name = "ms-event-category", path = "/api/v1/events")
=======
@FeignClient(name = "ms-event-category", contextId = "eventClient", path = "/api/v1/events")
>>>>>>> develop
public interface EventClient {

    @GetMapping("/{eventId}")
    HttpGlobalResponse<EventResponseDto> getEventById(@PathVariable("eventId") Long eventId);
}
