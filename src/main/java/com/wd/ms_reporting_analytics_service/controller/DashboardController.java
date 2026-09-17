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
 * RF-55: Panel de resumen. Acceso: cualquier usuario autenticado, pero
 * acotado a sus propios eventos.
 *
 * Resuelto: no existe (ni se va a crear) un rol "Administrador" global de
 * plataforma; el rol ADMIN de este sistema es por evento (user_event_roles
 * en ms-enrollment). Como este endpoint agrega datos de varios eventos a
 * la vez, no hay un evento puntual contra el cual comprobar ese rol, asi
 * que en vez de eso se filtra el agregado por ownerId == X-User-Id: cada
 * usuario ve el resumen de los eventos que el mismo organiza.
 */
@RestController
@RequestMapping("/reports/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/summary")
    public ResponseEntity<DashboardSummaryResponse> getSummary(
            @RequestHeader("X-User-Id") Long userId) {

        return ResponseEntity.ok(dashboardService.getSummary(userId));
    }
}