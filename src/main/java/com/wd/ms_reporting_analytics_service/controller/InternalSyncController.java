package com.wd.ms_reporting_analytics_service.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.wd.ms_reporting_analytics_service.domain.EventSummary;
import com.wd.ms_reporting_analytics_service.service.EventSummaryConsolidationService;

import lombok.RequiredArgsConstructor;

/**
 * Endpoint interno (punto B que elegiste: sin RabbitMQ/Kafka por ahora).
 * Los otros microservicios (o un scheduler tuyo con @Scheduled) llaman
 * esto cuando haya un cambio importante: se aprobo un cupo, se publico
 * un resultado, etc.
 *
 * Pendiente de decidir contigo: ¿este endpoint debe protegerse con un
 * header/secret interno para que solo otros microservicios lo llamen
 * (no cualquiera desde el Gateway)? En ms-enrollment usas X-User-Id/
 * X-User-Email inyectados por el gateway para el usuario final, pero
 * esto es trafico servicio-a-servicio, es un caso distinto.
 */
@RestController
@RequestMapping("/reports/internal/sync")
@RequiredArgsConstructor
public class InternalSyncController {

    private final EventSummaryConsolidationService consolidationService;

    @PostMapping("/{eventId}")
    public ResponseEntity<EventSummary> syncEvent(@PathVariable Long eventId) {
        return ResponseEntity.ok(consolidationService.syncEventSummary(eventId));
    }
}
