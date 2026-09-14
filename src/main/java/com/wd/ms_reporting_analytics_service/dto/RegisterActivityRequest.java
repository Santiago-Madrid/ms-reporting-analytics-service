package com.wd.ms_reporting_analytics_service.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RegisterActivityRequest {

    @NotBlank
    private String userId;

    @NotBlank
    private String userRole;

    @NotBlank
    private String action;

    private String description;

    @NotBlank
    private String entityAffected;

    private String entityId;

    private String ipAddress;

    private String userAgent;
}