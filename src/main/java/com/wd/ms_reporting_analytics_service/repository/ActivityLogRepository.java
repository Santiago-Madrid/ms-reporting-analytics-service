package com.wd.ms_reporting_analytics_service.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.wd.ms_reporting_analytics_service.domain.ActivityLog;

public interface ActivityLogRepository extends MongoRepository<ActivityLog, String> {
    List<ActivityLog> findByEntityAffectedAndEntityId(String entityAffected, String entityId);
}
