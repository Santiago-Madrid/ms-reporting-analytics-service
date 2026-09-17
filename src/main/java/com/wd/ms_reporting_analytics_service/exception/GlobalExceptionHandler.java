package com.wd.ms_reporting_analytics_service.exception;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Maneja solo las excepciones propias de los reportes nuevos (permisos y
 * disponibilidad del cronograma). No agrega un handler generico de
 * Exception.class a proposito: este @RestControllerAdvice aplica a TODOS
 * los controllers del microservicio (incluidos RankingController,
 * ActivityLogController, InternalSyncController), y no se quiere alterar
 * el comportamiento de errores que ya tenian esos endpoints.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ForbiddenReportAccessException.class)
    public ResponseEntity<Map<String, String>> handleForbidden(ForbiddenReportAccessException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", ex.getMessage()));
    }

    @ExceptionHandler(ScheduleNotAvailableException.class)
    public ResponseEntity<Map<String, String>> handleScheduleNotAvailable(ScheduleNotAvailableException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", ex.getMessage()));
    }
}
