package com.wd.ms_reporting_analytics_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** RF-55: resumen general del sistema para el panel administrativo. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardSummaryResponse {
    private long totalEvents;
    private int totalRegisteredUsers;
    private int totalApprovedEnrollments;
    private int totalModalities;
    private double totalRevenue;
    private double overallAverageScore;
}