package com.wd.ms_reporting_analytics_service.dto.external;

import java.time.Instant;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Copia local (ACL) de MusicTrackResponseDto de ms-music-media, con el
 * subconjunto de campos que necesita el reporte de "pista publicada".
 */
@Data
@NoArgsConstructor
public class MusicTrackDto {
    private Long enrollmentId;
    private String filename;
    private Boolean isActive;
    private Instant uploadedAt;
}
