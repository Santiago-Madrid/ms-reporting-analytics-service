package com.wd.ms_reporting_analytics_service.service;

import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;

import com.wd.ms_reporting_analytics_service.client.EventCategoryClient;
import com.wd.ms_reporting_analytics_service.client.ScoringClient;
import com.wd.ms_reporting_analytics_service.dto.external.ResultResponse;

import lombok.RequiredArgsConstructor;

/**
 * RF-50: ranking por categoria, en vivo (siempre pega a ms-scoring,
 * como definiste). No usa el read model event_summaries porque ese
 * puede estar desactualizado si el sync interno no se ha disparado.
 */
@Service
@RequiredArgsConstructor
public class RankingService {

    private final EventCategoryClient eventCategoryClient;
    private final ScoringClient scoringClient;

    /** Ranking de una sola modalidad/categoria puntual. */
    public List<ResultResponse> getRankingByModality(Long eventId, Long modalityId) {
        List<ResultResponse> results = scoringClient.getResultsByModality(
                String.valueOf(eventId),
                String.valueOf(modalityId));

        return results.stream()
                .sorted(Comparator.comparing(
                        ResultResponse::getFinalScore,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();
    }

    /** Ranking de TODAS las modalidades de un evento (una llamada por modalidad). */
    public java.util.Map<Long, List<ResultResponse>> getRankingByEvent(Long eventId) {
        var modalities = eventCategoryClient.getModalitiesByEventId(eventId).getData();

        return modalities.stream()
                .collect(java.util.stream.Collectors.toMap(
                        m -> m.getId(),
                        m -> getRankingByModality(eventId, m.getId())));
    }
}
