package com.wd.ms_reporting_analytics_service.service;

import java.io.ByteArrayOutputStream;

import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;

@Service
public class PdfGeneratorService {

    private final TemplateEngine templateEngine;
    private final ReportBrandingAssets brandingAssets;

    public PdfGeneratorService(TemplateEngine templateEngine, ReportBrandingAssets brandingAssets) {
        this.templateEngine = templateEngine;
        this.brandingAssets = brandingAssets;
    }

    /**
     * Renderiza la plantilla Thymeleaf indicada con el contexto dado y la
     * convierte a PDF real con openhtmltopdf. Inyecta siempre el logo de
     * marca en el contexto para que ningun template nuevo pueda olvidarlo.
     */
    public byte[] generatePdf(String templateName, Context context) {
        context.setVariable("logoDataUri", brandingAssets.getLogoDataUri());
        String html = templateEngine.process(templateName, context);

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            builder.withHtmlContent(html, null);
            builder.toStream(outputStream);
            builder.run();
            return outputStream.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Error generando el PDF del reporte", e);
        }
    }
}
