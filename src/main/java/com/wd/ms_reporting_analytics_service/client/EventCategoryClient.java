package com.wd.ms_reporting_analytics_service.client;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.wd.ms_reporting_analytics_service.dto.external.HttpGlobalResponse;
import com.wd.ms_reporting_analytics_service.dto.external.ModalityResponseDto;

/**
 * Cliente Feign hacia ms-event-category.
 *
 * OJO: el "name" debe coincidir EXACTO con spring.application.name
 * de ms-event-category (confirmado en su application.yml: "ms-event-category").
 * Este es justo el bug que tuviste en ms-enrollment con
 * "event-category-service" vs "ms-event-category" - aqui ya usamos
 * el nombre real desde el principio.
 *
 * El path incluye /api/v1 porque ms-event-category tiene
 * server.servlet.context-path: /api/v1, y una llamada Feign
 * directa (via Consul) NO pasa por el Gateway, que es el unico
 * que strippea el context-path para el filtro de seguridad.
 */
@FeignClient(name = "ms-event-category", contextId = "eventCategoryClient", path = "/api/v1/modality")
public interface EventCategoryClient {

    @GetMapping("/getModalitiesByEventId/{eventId}")
    HttpGlobalResponse<List<ModalityResponseDto>> getModalitiesByEventId(@PathVariable("eventId") Long eventId);
}
