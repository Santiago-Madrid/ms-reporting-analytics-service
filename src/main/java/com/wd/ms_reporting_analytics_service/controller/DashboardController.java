package com.wd.ms_reporting_analytics_service.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.wd.ms_reporting_analytics_service.dto.DashboardSummaryResponse;
import com.wd.ms_reporting_analytics_service.service.DashboardService;

import lombok.RequiredArgsConstructor;

/**
 * RF-55: Panel administrativo. Acceso: Administrador.
 *
 * TODO (pendiente de confirmar con Santiago): el sistema no tiene un
 * rol fijo "Administrador" en el JWT (los roles son contextuales:
 * organizador = dueno del evento, participante = inscrito aceptado).
 * Falta definir si existe un rol Administrador global (ej. un flag
 * isAdmin en ms-auth-identityservice) para poder restringir este
 * endpoint correctamente. Mientras tanto, SOLO exige autenticacion
 * (X-User-Id presente), sin validar rol.
 */
@RestController
@RequestMapping("/reports/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/summary")
    public ResponseEntity<DashboardSummaryResponse> getSummary(
            @RequestHeader("X-User-Id") String userId) {

        // TODO: validar rol Administrador una vez este definido (ver nota arriba)
        return ResponseEntity.ok(dashboardService.getSummary());
    }
}