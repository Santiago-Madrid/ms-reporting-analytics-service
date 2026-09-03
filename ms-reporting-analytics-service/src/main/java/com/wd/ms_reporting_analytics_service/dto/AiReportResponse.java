package com.wd.ms_reporting_analytics_service.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Contrato PROPUESTO para lo que debe devolver el MCP de Python
 * (opcion B de las 2 que le explicamos a Santiago: texto separado
 * por secciones, no un solo bloque). Cuando el endpoint real este
 * listo del lado de Python, confirmar que respeta este shape o
 * ajustar aqui.
 *
 * Ejemplo esperado:
 * {
 *   "introduccion": "El evento Copa Salsa 2026 tuvo una participacion...",
 *   "analisisPorModalidad": "La modalidad Urbano Solo presento el promedio...",
 *   "conclusion": "En general, el nivel de juzgamiento fue consistente..."
 * }
 */
@Data
@NoArgsConstructor
public class AiReportResponse {
    private String introduccion;
    private String analisisPorModalidad;
    private String conclusion;
}