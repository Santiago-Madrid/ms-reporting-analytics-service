package com.wd.ms_reporting_analytics_service.dto.external;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Copia local (ACL) del ModalityResponseDto real que vive en wd-lib-common.
 * category y division se dejan como String simple: no necesitamos el
 * enum tipado aqui, solo el nombre para desnormalizarlo dentro de
 * modalities_breakdown en Mongo.
 */
@Data
@NoArgsConstructor
public class ModalityResponseDto {
    private Long id;
    private Long eventId;
    private String category;
    private String division;
    private Long minAge;
    private Long maxAge;
    private String style;
}