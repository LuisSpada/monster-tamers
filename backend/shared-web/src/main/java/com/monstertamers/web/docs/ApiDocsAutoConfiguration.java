package com.monstertamers.web.docs;

import io.swagger.v3.oas.models.OpenAPI;
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

    /**
     * Relative server URL: "Try it out" calls the host the docs were loaded from,
     * the gateway or the service itself.
     */
    @Bean
    @ConditionalOnMissingBean
    OpenAPI openApi() {
        return new OpenAPI().servers(List.of(new Server().url("/")));
    }
}
