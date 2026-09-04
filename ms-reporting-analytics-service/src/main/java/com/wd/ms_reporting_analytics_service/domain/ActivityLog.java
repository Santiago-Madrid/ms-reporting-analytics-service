package com.wd.ms_reporting_analytics_service.domain;

import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Document(collection = "activity_logs")
public class ActivityLog {

    @Id
    private String id;

    /** Solo el id, nunca nombre/correo -> cumple Ley 1581 de 2012 */
    private String userId;

    private String userRole;

    /** Ej: DELETE_EVENT, CHANGE_USER_ROLE, PUBLISH_RESULTS */
    private String action;

    private String description;

    private String entityAffected;

    private String entityId;

    private Metadata metadata = new Metadata();

    private Instant timestamp = Instant.now();

    @Data
    @NoArgsConstructor
    public static class Metadata {
        private String ipAddress;
        private String userAgent;
    }
}
