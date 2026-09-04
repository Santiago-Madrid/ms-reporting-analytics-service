package com.wd.ms_reporting_analytics_service.dto.external;

import java.time.Instant;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Copia local (ACL) de ResultResponse que vive en ms-scoring.
 * "status" se deja como String (en vez del enum ResultStatus real)
 * porque ms-reports no necesita la logica de negocio del enum,
 * solo mostrar/filtrar por su valor.
 */
@Data
@NoArgsConstructor
public class ResultResponse {
    private String id;
    private String eventId;
    private String modalityId;
    private String enrollmentId;
    private String participantName;
    private Double finalScore;
    private Integer ranking;
    private String status;
    private Instant publishedAt;
    private Instant createdAt;
}