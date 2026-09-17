package com.wd.ms_reporting_analytics_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

import com.wd.ms_reporting_analytics_service.dto.external.HttpGlobalResponse;
import com.wd.ms_reporting_analytics_service.dto.external.MusicTrackDto;

/**
 * Cliente Feign hacia ms-music-media, usado solo por el reporte de
 * "pista publicada / no publicada". Se consulta metadata por inscripcion
 * (no existe un endpoint bulk hoy y no se crea uno para no modificar
 * ms-music-media/wd-lib-common). El X-User-Id que se reenvia debe ser el
 * del usuario real que pidio el reporte: ya fue validado como dueno o
 * EventRole.ADMIN del evento por ReportAccessGuard, y ese mismo criterio
 * es el que exige el chequeo de permisos interno de ms-music-media
 * (validateReadPermission), asi que la llamada se autoriza sin cambios
 * en ese servicio.
 */
@FeignClient(name = "ms-music-media", contextId = "musicMediaClient", path = "/api/v1/music")
public interface MusicMediaClient {

    @GetMapping("/metadata/{enrollmentId}")
    HttpGlobalResponse<MusicTrackDto> getMetadata(
            @PathVariable("enrollmentId") Long enrollmentId,
            @RequestHeader("X-User-Id") Long userId);
}
