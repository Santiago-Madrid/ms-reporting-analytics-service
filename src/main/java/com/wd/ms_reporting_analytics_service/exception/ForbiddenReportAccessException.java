package com.wd.ms_reporting_analytics_service.exception;

/**
 * Se lanza cuando quien pide un reporte no es ni el dueno (ownerId) del
 * evento ni tiene EventRole.ADMIN asignado en ese evento.
 */
public class ForbiddenReportAccessException extends RuntimeException {
    public ForbiddenReportAccessException(String message) {
        super(message);
    }
}
