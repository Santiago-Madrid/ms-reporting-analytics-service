package com.wd.ms_reporting_analytics_service.dto.external;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Copia local (ACL) del EventResponseDto real de wd_lib_common.
 *
 * OJO: el campo real se llama "IdEvent" (con I mayuscula), pero Jackson
 * lo serializa como "idEvent" siguiendo la convencion estandar de
 * JavaBeans (getIdEvent() -> propiedad "idEvent"). Por eso aqui usamos
 * camelCase normal, no el nombre literal del campo original.
 *
 * "status" se deja como String (en vez del enum Status real) por la
 * misma razon que en ResultResponse: ms-reports no necesita la logica
 * de negocio del enum, solo mostrarlo/usarlo como dato.
 */
@Data
@NoArgsConstructor
public class EventResponseDto {
    private Long idEvent;
    private Long ownerId;
    private String name;
    private String description;
    private String startDate;
    private String endDate;
    private String location;
    private String status;
    private String message;
}
