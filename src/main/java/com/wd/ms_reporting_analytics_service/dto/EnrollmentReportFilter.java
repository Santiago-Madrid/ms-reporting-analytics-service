package com.wd.ms_reporting_analytics_service.dto;

/**
 * Filtro para el reporte de inscritos de un evento.
 * WITH_TRACK/WITHOUT_TRACK solo tienen sentido sobre inscripciones
 * APPROVED (solo esas pueden tener una pista musical subida).
 */
public enum EnrollmentReportFilter {
    ALL,
    APPROVED,
    NOT_APPROVED,
    WITH_TRACK,
    WITHOUT_TRACK
}
