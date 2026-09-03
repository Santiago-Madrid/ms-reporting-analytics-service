package com.wd.ms_reporting_analytics_service.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.wd.ms_reporting_analytics_service.domain.EventSummary;
import com.wd.ms_reporting_analytics_service.dto.DashboardSummaryResponse;
import com.wd.ms_reporting_analytics_service.repository.EventSummaryRepository;

import lombok.RequiredArgsConstructor;

/**
 * RF-55: Panel administrativo - resumen general del sistema.
 * A proposito lee del read model (event_summaries) y NO le pega en vivo
 * a ms-event-category/ms-scoring: es justo el patron de Vista Consolidada
 * que definiste para evitar saturar los servicios transaccionales cuando
 * el admin entra al dashboard.
 */
@Service
@RequiredArgsConstructor
public class DashboardService {

    private final EventSummaryRepository eventSummaryRepository;

    public DashboardSummaryResponse getSummary() {
        List<EventSummary> allSummaries = eventSummaryRepository.findAll();

        long totalEvents = allSummaries.size();
        int totalRegisteredUsers = allSummaries.stream()
                .mapToInt(s -> s.getTotals().getRegisteredUsers())
                .sum();
        int totalApprovedEnrollments = allSummaries.stream()
                .mapToInt(s -> s.getTotals().getApprovedEnrollments())
                .sum();
        int totalModalities = allSummaries.stream()
                .mapToInt(s -> s.getTotals().getTotalModalities())
                .sum();
        double totalRevenue = allSummaries.stream()
                .mapToDouble(s -> s.getTotals().getTotalRevenue())
                .sum();
        double overallAverageScore = allSummaries.stream()
                .mapToDouble(s -> s.getEvaluationMetrics().getOverallAverage())
                .average()
                .orElse(0.0);

        return new DashboardSummaryResponse(
                totalEvents,
                totalRegisteredUsers,
                totalApprovedEnrollments,
                totalModalities,
                totalRevenue,
                overallAverageScore);
    }
}
