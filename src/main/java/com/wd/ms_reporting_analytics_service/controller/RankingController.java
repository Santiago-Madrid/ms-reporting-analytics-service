package com.wd.ms_reporting_analytics_service.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.wd.ms_reporting_analytics_service.dto.external.ResultResponse;
import com.wd.ms_reporting_analytics_service.service.RankingService;

import lombok.RequiredArgsConstructor;

/** RF-50: Consulta de resultados - ranking por categoria. Acceso: Todos. */
@RestController
@RequestMapping("/reports/events/{eventId}")
@RequiredArgsConstructor
public class RankingController {

    private final RankingService rankingService;

    @GetMapping("/modalities/{modalityId}/ranking")
    public ResponseEntity<List<ResultResponse>> getRankingByModality(
            @PathVariable Long eventId,
            @PathVariable Long modalityId) {
        return ResponseEntity.ok(rankingService.getRankingByModality(eventId, modalityId));
    }

    @GetMapping("/ranking")
    public ResponseEntity<Map<Long, List<ResultResponse>>> getRankingByEvent(
            @PathVariable Long eventId) {
        return ResponseEntity.ok(rankingService.getRankingByEvent(eventId));
    }
}
