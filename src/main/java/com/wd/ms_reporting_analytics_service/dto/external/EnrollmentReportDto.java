package com.wd.ms_reporting_analytics_service.dto.external;

import java.time.LocalDateTime;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Copia local (ACL) de EnrollmentResponseDto de ms-enrollment, con el
 * subconjunto de campos que necesitan los reportes de inscritos.
 * No se importa wd-lib-common (ver nota en HttpGlobalResponse.java).
 */
@Data
@NoArgsConstructor
public class EnrollmentReportDto {
    private Long enrollmentId;
    private Long userId;
    private Long eventId;
    private Long modalityId;
    private String status;
    private LocalDateTime createdAt;
    private ParticipantDto participant;

    @Data
    @NoArgsConstructor
    public static class ParticipantDto {
        private Long id;
        private String name;
        private String lastName;
        private String email;
        private String documentNumber;
    }
}
