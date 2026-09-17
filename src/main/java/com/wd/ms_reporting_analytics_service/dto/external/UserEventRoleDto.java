package com.wd.ms_reporting_analytics_service.dto.external;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Copia local (ACL) de UserEventRoleResponseDto de ms-enrollment.
 * roleInEvent llega como String (nombre del enum EventRole) para no
 * acoplar este servicio al enum real de wd-lib-common.
 */
@Data
@NoArgsConstructor
public class UserEventRoleDto {
    private Long id;
    private Long userId;
    private Long eventId;
    private String roleInEvent;
}
