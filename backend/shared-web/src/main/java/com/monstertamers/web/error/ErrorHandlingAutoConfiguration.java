package com.monstertamers.web.error;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.web.servlet.HandlerExceptionResolver;

/** Turns on the common error format in every service that has this module. */
@AutoConfiguration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class ErrorHandlingAutoConfiguration {

    @Bean
    GlobalExceptionHandler globalExceptionHandler() {
        return new GlobalExceptionHandler();
    }

    /** Only in services with Spring Security. */
    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass(AuthenticationEntryPoint.class)
    static class SecurityErrorHandling {

        @Bean
        SecurityExceptionHandler securityExceptionHandler() {
            return new SecurityExceptionHandler();
        }

        @Bean
        SecurityErrorHandler securityErrorHandler(
                @Qualifier("handlerExceptionResolver") HandlerExceptionResolver resolver
        ) {
            return new SecurityErrorHandler(resolver);
        }
    }
}
