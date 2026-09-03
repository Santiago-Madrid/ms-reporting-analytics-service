package com.wd.ms_reporting_analytics_service.service;

import java.time.Instant;
import java.util.List;

import org.springframework.stereotype.Service;

import com.wd.ms_reporting_analytics_service.domain.ActivityLog;
import com.wd.ms_reporting_analytics_service.dto.RegisterActivityRequest;
import com.wd.ms_reporting_analytics_service.repository.ActivityLogRepository;

import lombok.RequiredArgsConstructor;

/**
 * RF-60: Registro de actividad basica.
 * Quien llama esto es "Sistema" (segun tu tabla de RFs), es decir,
 * otros microservicios cuando ocurre una accion importante
 * (crear evento, publicar resultados, etc.), NO el usuario final
 * directamente. Por eso no valida rol de usuario aqui adentro.
 */
@Service
@RequiredArgsConstructor
public class ActivityLogService {

    private final ActivityLogRepository activityLogRepository;

    public ActivityLog register(RegisterActivityRequest request) {
        ActivityLog log = new ActivityLog();
        log.setUserId(request.getUserId());
        log.setUserRole(request.getUserRole());
        log.setAction(request.getAction());
        log.setDescription(request.getDescription());
        log.setEntityAffected(request.getEntityAffected());
        log.setEntityId(request.getEntityId());

        ActivityLog.Metadata metadata = new ActivityLog.Metadata();
        metadata.setIpAddress(request.getIpAddress());
        metadata.setUserAgent(request.getUserAgent());
        log.setMetadata(metadata);

        log.setTimestamp(Instant.now());

        return activityLogRepository.save(log);
    }

    /** Trazabilidad: historial de acciones sobre un recurso puntual
     *  (ej. todos los cambios sobre un evento especifico). */
    public List<ActivityLog> getByEntity(String entityAffected, String entityId) {
        return activityLogRepository.findByEntityAffectedAndEntityId(entityAffected, entityId);
    }
}
