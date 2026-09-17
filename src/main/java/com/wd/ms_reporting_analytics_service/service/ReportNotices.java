package com.wd.ms_reporting_analytics_service.service;

import com.wd.ms_reporting_analytics_service.domain.EventSummary;

/**
 * Mensajes explicativos para cuando un reporte no tiene datos que mostrar
 * (sin inscritos aprobados, sin calificaciones, sin cronograma generado),
 * en vez de dejar que el lector interprete un simple "0" como un error o
 * como un puntaje real de cero.
 */
public final class ReportNotices {

    private ReportNotices() { }

    public static String participantsNotice(EventSummary summary) {
        Integer approved = summary.getTotals() != null ? summary.getTotals().getApprovedEnrollments() : null;
        if (approved == null || approved == 0) {
            return "Este evento todavía no tiene inscripciones aprobadas registradas.";
        }
        return null;
    }

    public static String scoringNotice(EventSummary summary) {
        boolean hasResults = summary.getEvaluationMetrics() != null
                && Boolean.TRUE.equals(summary.getEvaluationMetrics().getHasResults());
        if (!hasResults) {
            return "Todavía no hay calificaciones finales registradas para este evento. "
                    + "El promedio, el puntaje máximo y el mínimo se actualizarán cuando los jurados completen sus evaluaciones.";
        }
        return null;
    }

    public static String scheduleNotice(String scheduleStatus) {
        String status = scheduleStatus == null ? "NOT_CONFIGURED" : scheduleStatus;
        return switch (status) {
            case "CONFIGURED", "AVAILABLE" -> null;
            case "NOT_CONFIGURED" -> "El cronograma de este evento todavía no ha sido generado.";
            case "DRAFT_NOT_VISIBLE" -> "El cronograma existe pero está en borrador: no es visible hasta que el organizador lo publique.";
            default -> "No se pudo consultar el estado del cronograma en este momento.";
        };
    }
}
