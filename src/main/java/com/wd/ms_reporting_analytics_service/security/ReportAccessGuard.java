package com.wd.ms_reporting_analytics_service.security;

import org.springframework.stereotype.Component;

import com.wd.ms_reporting_analytics_service.client.EnrollmentClient;
import com.wd.ms_reporting_analytics_service.client.EventClient;
import com.wd.ms_reporting_analytics_service.dto.external.EventResponseDto;
import com.wd.ms_reporting_analytics_service.dto.external.HttpGlobalResponse;
import com.wd.ms_reporting_analytics_service.dto.external.UserEventRoleDto;
import com.wd.ms_reporting_analytics_service.exception.ForbiddenReportAccessException;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Autorizacion de los reportes: solo el dueno del evento (Event.ownerId)
 * o un usuario con EventRole.ADMIN asignado en ese evento (tabla
 * user_event_roles de ms-enrollment) pueden pedirlos.
 *
 * A diferencia de EventSummaryConsolidationService/RankingService (que
 * toleran caidas de otros servicios con valores por defecto, correcto
 * para *datos*), esta clase falla cerrado: cualquier duda se traduce en
 * acceso denegado, nunca en acceso concedido.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ReportAccessGuard {

    private static final String EVENT_ADMIN_ROLE = "ADMIN";

    private final EventClient eventClient;
    private final EnrollmentClient enrollmentClient;

    public void assertOwnerOrEventAdmin(Long eventId, Long authenticatedUserId) {
        if (eventId == null || authenticatedUserId == null) {
            throw new ForbiddenReportAccessException("No se pudo identificar el evento o el usuario autenticado.");
        }

        EventResponseDto event;
        try {
            HttpGlobalResponse<EventResponseDto> response = eventClient.getEventById(eventId);
            event = response != null ? response.getData() : null;
        } catch (Exception e) {
            log.warn("No se pudo verificar el dueno del evento {} (falla al consultar ms-event-category): {}", eventId, e.getMessage());
            throw new ForbiddenReportAccessException("No se pudo verificar el acceso al reporte del evento " + eventId + ".");
        }

        if (event == null) {
            throw new ForbiddenReportAccessException("No se encontro el evento con id " + eventId + ".");
        }

        if (event.getOwnerId() != null && event.getOwnerId().equals(authenticatedUserId)) {
            return;
        }

        if (isEventAdmin(eventId, authenticatedUserId)) {
            return;
        }

        throw new ForbiddenReportAccessException(
                "Acceso denegado: solo el organizador dueno del evento o un administrador del evento pueden acceder a este reporte.");
    }

    private boolean isEventAdmin(Long eventId, Long authenticatedUserId) {
        try {
            UserEventRoleDto role = enrollmentClient.getUserEventRole(eventId, authenticatedUserId);
            return role != null && EVENT_ADMIN_ROLE.equalsIgnoreCase(role.getRoleInEvent());
        } catch (FeignException.NotFound e) {
            // El usuario no tiene ningun rol asignado en este evento: resultado valido, no es admin.
            return false;
        } catch (Exception e) {
            log.warn("No se pudo verificar el rol de evento del usuario {} en el evento {}: {}", authenticatedUserId, eventId, e.getMessage());
            return false;
        }
    }
}
