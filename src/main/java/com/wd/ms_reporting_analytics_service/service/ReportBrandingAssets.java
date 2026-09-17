package com.wd.ms_reporting_analytics_service.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * Carga el logo oficial de World Dance una unica vez al iniciar el
 * servicio y lo deja listo como data URI base64. openhtmltopdf renderiza
 * el HTML con base URI nula (ver PdfGeneratorService), asi que cualquier
 * imagen debe ir embebida en el propio HTML, no como una ruta relativa.
 */
@Slf4j
@Component
public class ReportBrandingAssets {

    @Getter
    private final String logoDataUri;

    public ReportBrandingAssets() {
        this.logoDataUri = loadLogoAsDataUri();
    }

    private String loadLogoAsDataUri() {
        try (InputStream in = new ClassPathResource("branding/wd-logo.png").getInputStream()) {
            byte[] bytes = in.readAllBytes();
            return "data:image/png;base64," + Base64.getEncoder().encodeToString(bytes);
        } catch (IOException e) {
            log.warn("No se pudo cargar el logo de World Dance para los reportes: {}", e.getMessage());
            return "";
        }
    }
}
