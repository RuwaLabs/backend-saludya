package com.ruwalabs.saludya.shared.infrastructure.documentation.openapi.configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configures the OpenAPI specification exposed by SaludYa.
 */
@Configuration
public class OpenApiConfiguration {

    private static final String API_TITLE = "SaludYa API";
    private static final String API_DESCRIPTION = "Backend API de SaludYa - plataforma de gestión de citas médicas y "
            + "control de sala de espera para establecimientos públicos de salud. Expone los bounded contexts "
            + "Identity & Access Management, Appointments & Booking, Reassignment, Arrival & QR Check-in y "
            + "Hospital Operations & Configuration.";
    private static final String API_VERSION = "v1.0.0";
    private static final String SECURITY_SCHEME_NAME = "bearerAuth";

    /**
     * Builds the OpenAPI document used by Swagger UI and client generation tools.
     *
     * @return configured OpenAPI descriptor
     */
    @Bean
    public OpenAPI saludyaOpenApi() {

        var openApi = new OpenAPI();

        openApi
                .info(new Info()
                        .title(API_TITLE)
                        .description(API_DESCRIPTION)
                        .version(API_VERSION)
                        .contact(new Contact()
                                .name("RuwaLabs")
                                .url("https://github.com/RuwaLabs"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0.html")));

        // No explicit servers: Swagger UI resolves requests against the host that
        // serves it, so "Try it out" works behind any host (localhost, public IP, domain).
        openApi
                .addSecurityItem(new SecurityRequirement()
                        .addList(SECURITY_SCHEME_NAME))
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME,
                                new SecurityScheme()
                                        .name(SECURITY_SCHEME_NAME)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("JWT Bearer token for API authentication")));

        return openApi;
    }
}
