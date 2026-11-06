package com.monstertamers.web.error;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.handler.HandlerExceptionResolverComposite;

class ErrorHandlingAutoConfigurationTest {

    @Test
    @DisplayName("A service without Spring Security starts without the security handlers")
    void withoutSecurity_succeeds() {
        // given
        WebApplicationContextRunner runner = contextRunner()
            .withClassLoader(withoutSecurity());

        // when
        runner.run(context -> {
            // then
            assertThat(context).hasNotFailed();
            assertThat(context).doesNotHaveBean(SecurityExceptionHandler.class);
            assertThat(context).doesNotHaveBean(SecurityErrorHandler.class);
        });
    }

    @Test
    @DisplayName("A service with Spring Security receives the security handlers")
    void withSecurity_succeeds() {
        // given
        WebApplicationContextRunner runner = contextRunner();

        // when
        runner.run(context -> {
            // then
            assertThat(context).hasSingleBean(SecurityExceptionHandler.class);
            assertThat(context).hasSingleBean(SecurityErrorHandler.class);
        });
    }

    /** Prepares a servlet app with only our auto-configuration and the resolver Spring MVC normally registers. */
    private static WebApplicationContextRunner contextRunner() {
        return new WebApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(ErrorHandlingAutoConfiguration.class))
                .withBean("handlerExceptionResolver", HandlerExceptionResolver.class, HandlerExceptionResolverComposite::new);
    }

    /** Hides Spring Security as if the service had no dependency on it. */
    private static FilteredClassLoader withoutSecurity() {
        return new FilteredClassLoader("org.springframework.security");
    }
}
