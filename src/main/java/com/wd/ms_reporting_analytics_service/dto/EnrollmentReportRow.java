package com.wd.ms_reporting_analytics_service.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Una fila del reporte de inscritos. Los campos *Label/*CssClass ya vienen
 * resueltos desde el servicio para que las plantillas Thymeleaf no tengan
 * que evaluar condicionales/ternarios (mas fragil de mantener en HTML).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnrollmentReportRow {
    private Long enrollmentId;
    private String fullName;
    private String initials;
    private String documentNumber;
    private String email;
    private String modalityName;
    private String status;
    private String statusLabel;
    private String statusCssClass;
    private Boolean hasMusicTrack;
    private String trackLabel;
    private String trackCssClass;
    private LocalDateTime createdAt;
}
