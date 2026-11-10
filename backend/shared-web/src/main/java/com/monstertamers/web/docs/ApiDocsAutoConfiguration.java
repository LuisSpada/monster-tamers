package com.monstertamers.web.docs;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import java.util.List;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;

/** Common API docs setup in every service that has springdoc. */
@AutoConfiguration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnClass(OpenAPI.class)
public class ApiDocsAutoConfiguration {

    private static final String BEARER_AUTH = "bearerAuth";

    /**
     * Relative server URL: "Try it out" calls the host the docs were loaded from,
     * the gateway or the service itself.
     */
    @Bean
    @ConditionalOnMissingBean
    OpenAPI openApi() {

        Server server = new Server().url("/");
        SecurityScheme bearerAuth = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT");

        return new OpenAPI()
                .servers(List.of(server))
                .components(new Components().addSecuritySchemes(BEARER_AUTH, bearerAuth))
                .addSecurityItem(new SecurityRequirement().addList(BEARER_AUTH));
    }
}
