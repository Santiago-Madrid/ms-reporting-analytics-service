package com.wd.ms_reporting_analytics_service.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.wd.ms_reporting_analytics_service.domain.ActivityLog;
import com.wd.ms_reporting_analytics_service.dto.RegisterActivityRequest;
import com.wd.ms_reporting_analytics_service.service.ActivityLogService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * RF-60: Registro de actividad basica.
 * Este endpoint lo llaman OTROS microservicios (rol "Sistema"), no
 * el frontend directamente. Igual que con /reports/internal/sync,
 * pendiente de definir contigo si se protege con un secret interno.
 */
@RestController
@RequestMapping("/reports/activity-logs")
@RequiredArgsConstructor
public class ActivityLogController {

    private final ActivityLogService activityLogService;

    @PostMapping
    public ResponseEntity<ActivityLog> register(@Valid @RequestBody RegisterActivityRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(activityLogService.register(request));
    }

    /** Para que el Administrador audite un recurso puntual, ej:
     *  GET /reports/activity-logs?entityAffected=EVENT&entityId=12 */
    @GetMapping
    public ResponseEntity<List<ActivityLog>> getByEntity(
            @RequestParam String entityAffected,
            @RequestParam String entityId) {
        return ResponseEntity.ok(activityLogService.getByEntity(entityAffected, entityId));
    }
}
