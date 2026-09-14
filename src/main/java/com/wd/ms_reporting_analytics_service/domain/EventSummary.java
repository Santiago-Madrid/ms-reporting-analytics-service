package com.wd.ms_reporting_analytics_service.domain;

import java.time.Instant;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Document(collection = "event_summaries")
public class EventSummary {

    @Id
    private String id;

    /** Referencia logica a ms-event-category. Se guarda como String
     *  aunque alla sea Long, para no acoplar el tipo de Mongo a MySQL. */
    private String eventId;

    /** Id del organizador dueno del evento (util para mas adelante,
     *  si se valida que solo el organizador dueno o un admin pueda
     *  pedir el reporte de este evento). */
    private Long ownerId;

    /** Desnormalizado desde ms-event-category para no tener que
     *  consultarlo de nuevo solo para mostrar el nombre. */
    private String eventName;

    /** Se guarda como String tal cual viene de ms-event-category
     *  (startDate), para no arriesgar errores de parseo de formato
     *  de fecha entre microservicios. */
    private String executionDate;

    private Totals totals = new Totals();

    private List<ModalityBreakdown> modalitiesBreakdown;

    private EvaluationMetrics evaluationMetrics = new EvaluationMetrics();

    private ScheduleMetrics scheduleMetrics = new ScheduleMetrics();

    private SystemMetrics systemMetrics = new SystemMetrics();

    private Instant generatedAt;

    /** DRAFT | LIVE | CONSOLIDATED */
    private String status = "DRAFT";

    @Data
    @NoArgsConstructor
    public static class Totals {
        private Integer registeredUsers = 0;
        private Integer approvedEnrollments = 0;
        private Integer totalModalities = 0;
        private Double totalRevenue = 0.0;
    }

    @Data
    @NoArgsConstructor
    public static class ModalityBreakdown {
        private Long modalityId;
        private String category;
        private String division;
        private Integer participantCount = 0;
        private Double averageScore = 0.0;
    }

    @Data
    @NoArgsConstructor
    public static class EvaluationMetrics {
        private Double highestScore = 0.0;
        private Double lowestScore = 0.0;
        private Double overallAverage = 0.0;
    }

    @Data
    @NoArgsConstructor
    public static class ScheduleMetrics {
        private Integer totalSlots = 0;
        private String scheduleStatus = "NOT_CONFIGURED";
        private String generatedAt;
    }

    @Data
    @NoArgsConstructor
    public static class SystemMetrics {
        private Integer peakLiveViewers = 0;
        private Integer totalStreamReactions = 0;
    }
}