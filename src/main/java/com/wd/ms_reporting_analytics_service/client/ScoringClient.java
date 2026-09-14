package com.wd.ms_reporting_analytics_service.client;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.wd.ms_reporting_analytics_service.dto.external.ResultResponse;

/**
 * Cliente Feign hacia ms-scoring.
 *
 * name = "ms-scoring" -> confirmado en su application.yml
 * (spring.application.name), tal cual quedo registrado en Consul.
 *
 * path incluye /api/v1/scoring porque:
 *   - context-path del servicio es /api/v1
 *   - el @RequestMapping base de ResultController es /scoring/events/{eventId}/modalities/{modalityId}/results
 *
 * OJO: eventId y modalityId aqui son String (asi los tiene ResultResponse
 * y el ResultController), a diferencia de ms-event-category donde son Long.
 * Cuando se recorra la lista de modalidades de un evento (ModalityResponseDto.id
 * es Long) hay que convertir con String.valueOf(...) antes de llamar este metodo.
 */
@FeignClient(name = "ms-scoring", path = "/api/v1/scoring")
public interface ScoringClient {

    @GetMapping("/events/{eventId}/modalities/{modalityId}/results")
    List<ResultResponse> getResultsByModality(
            @PathVariable("eventId") String eventId,
            @PathVariable("modalityId") String modalityId);
}