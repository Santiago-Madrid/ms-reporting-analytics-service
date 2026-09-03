package com.wd.ms_reporting_analytics_service.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.wd.ms_reporting_analytics_service.domain.EventSummary;

public interface EventSummaryRepository extends MongoRepository<EventSummary, String> {
    Optional<EventSummary> findByEventId(String eventId);
}
