package com.wd.ms_reporting_analytics_service.dto.external;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Copia local del wrapper que usa ms-event-category (HttpGlobalResponse
 * de wd-lib-common).
 *
 * A PROPOSITO no importamos wd-lib-common como dependencia: ya identificaste
 * que esa libreria arrastra JPA/servlet transitivamente y te dio problemas
 * en ms-scoring-evaluation y api-gateway (los dos son Mongo/WebFlux puros).
 * ms-reports es igual de sensible por ser Mongo puro, asi que en vez de
 * pelear con exclusiones de Maven otra vez, simplemente duplicamos aqui
 * el shape minimo que necesitamos leer. Es un Anti-Corruption Layer:
 * si el DTO de ms-event-category cambia, solo se rompe este archivo,
 * no el resto de ms-reports.
 */
@Data
@NoArgsConstructor
public class HttpGlobalResponse<T> {
    private String message;
    private T data;
}