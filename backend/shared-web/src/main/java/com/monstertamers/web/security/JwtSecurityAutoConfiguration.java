package com.monstertamers.web.security;

import com.monstertamers.web.error.SecurityErrorHandler;
import jakarta.servlet.DispatcherType;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.security.autoconfigure.actuate.web.servlet.ManagementWebSecurityAutoConfiguration;
import org.springframework.boot.security.autoconfigure.web.servlet.ServletWebSecurityAutoConfiguration;
import org.springframework.boot.security.oauth2.server.resource.autoconfigure.OAuth2ResourceServerAutoConfiguration;
import org.springframework.boot.security.oauth2.server.resource.autoconfigure.web.OAuth2ResourceServerWebSecurityAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;

/** Lets in only requests with a valid access token, in every service that has the resource server. */
@AutoConfiguration(before = {
        ManagementWebSecurityAutoConfiguration.class,
        ServletWebSecurityAutoConfiguration.class,
        OAuth2ResourceServerAutoConfiguration.class,
        OAuth2ResourceServerWebSecurityAutoConfiguration.class
})
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnClass(BearerTokenAuthenticationFilter.class)
@EnableConfigurationProperties({JwtProperties.class, PublicPathsProperties.class})
public class JwtSecurityAutoConfiguration {

    /** Open in every service. */
    private static final String[] SERVICE_PATHS = {
            "/actuator/health",
            "/actuator/health/**",
            "/swagger-ui.html",
            "/swagger-ui/**",
            "/v3/api-docs",
            "/v3/api-docs/**"
    };

    /** Same secret as auth-service signs tokens with. */
    @Bean
    @ConditionalOnMissingBean
    JwtDecoder jwtDecoder(JwtProperties properties) {
        return JwtDecoderFactory.create(properties);
    }

    @Bean
    @ConditionalOnMissingBean
    SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            SecurityErrorHandler errorHandler,
            PublicPathsProperties publicPaths
    ) {
        return http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> {
                    // An exception thrown in a filter, before controllers, is forwarded to /error.
                    // Without this, Security blocks that forward and the 500 shows up as 401/403.
                    auth.dispatcherTypeMatchers(DispatcherType.ERROR).permitAll();
                    auth.requestMatchers(SERVICE_PATHS).permitAll();
                    if (!publicPaths.publicPaths().isEmpty()) {
                        auth.requestMatchers(publicPaths.publicPaths().toArray(String[]::new)).permitAll();
                    }
                    auth.anyRequest().authenticated();
                })
                .oauth2ResourceServer(server -> server
                        .jwt(jwt -> {})
                        .authenticationEntryPoint(errorHandler)
                        .accessDeniedHandler(errorHandler))
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(errorHandler)
                        .accessDeniedHandler(errorHandler))
                .build();
    }
}
