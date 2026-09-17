package com.wd.ms_reporting_analytics_service.client;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.wd.ms_reporting_analytics_service.dto.external.EnrollmentReportDto;
import com.wd.ms_reporting_analytics_service.dto.external.UserEventRoleDto;

/**
 * Cliente Feign hacia ms-enrollment, usado solo por los reportes nuevos
 * (listado de inscritos y verificacion de EventRole.ADMIN). No reemplaza
 * ni modifica ningun cliente existente de este servicio.
 *
 * name = "ms-enrollment" -> spring.application.name real (confirmado en
 * su application.yaml). path = "/api/v1/enrollments" -> su context-path
 * (/api/v1) + el @RequestMapping base del EnrollmentController.
 */
@FeignClient(name = "ms-enrollment", contextId = "enrollmentClient", path = "/api/v1/enrollments")
public interface EnrollmentClient {

    @GetMapping("/event/{eventId}")
    List<EnrollmentReportDto> getEnrollmentsByEvent(@PathVariable("eventId") Long eventId);

    /**
     * Devuelve 404 si el usuario no tiene un rol asignado en el evento; esto
     * es un resultado valido (no un error), tal como lo documenta el propio
     * EnrollmentController.getUserEventRole en ms-enrollment.
     */
    @GetMapping("/events/{eventId}/users/{userId}/role")
    UserEventRoleDto getUserEventRole(@PathVariable("eventId") Long eventId, @PathVariable("userId") Long userId);
}
