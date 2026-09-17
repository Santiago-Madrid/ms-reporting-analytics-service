package com.wd.ms_reporting_analytics_service.exception;

/**
 * Se lanza al pedir el PDF del cronograma de un evento que todavia no
 * tiene un cronograma generado (o cuyo borrador no es visible para quien
 * lo solicita).
 */
public class ScheduleNotAvailableException extends RuntimeException {
    public ScheduleNotAvailableException(String message) {
        super(message);
    }
}
