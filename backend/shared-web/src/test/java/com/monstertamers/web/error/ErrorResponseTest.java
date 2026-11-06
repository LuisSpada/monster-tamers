package com.monstertamers.web.error;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

class ErrorResponseTest {

    // Advice order comes from their @Order, as in a real service.
    private final MockMvcTester mvc = MockMvcTester.create(MockMvcBuilders
            .standaloneSetup(new ThrowingController())
            .setControllerAdvice(new GlobalExceptionHandler(), new SecurityExceptionHandler())
            .build());

    @Test
    @DisplayName("An invalid request body returns the invalid fields")
    void validation_rejected() {
        // given
        MockMvcTester.MockMvcRequestBuilder request = mvc.post()
                .uri("/validated")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}");

        // when
        MvcTestResult result = request.exchange();

        // then
        assertThat(result).hasStatus(HttpStatus.BAD_REQUEST);
        assertThat(result).hasContentType(MediaType.APPLICATION_PROBLEM_JSON);
        assertThat(result).bodyJson().extractingPath("$.code").isEqualTo("common.validation_failed");
        assertThat(result).bodyJson().extractingPath("$.errors[0].field").isEqualTo("name");
    }

    @Test
    @DisplayName("An ApiException returns the status and code of its ErrorCode")
    void apiException_rejected() {
        // given
        MockMvcTester.MockMvcRequestBuilder request = mvc.get().uri("/api-exception");

        // when
        MvcTestResult result = request.exchange();

        // then
        assertThat(result).hasStatus(HttpStatus.CONFLICT);
        assertThat(result).hasContentType(MediaType.APPLICATION_PROBLEM_JSON);
        assertThat(result).bodyJson().extractingPath("$.code").isEqualTo("test.conflict");
    }

    @Test
    @DisplayName("A failed authentication returns 401")
    void unauthenticated_rejected() {
        // given
        MockMvcTester.MockMvcRequestBuilder request = mvc.get().uri("/unauthenticated");

        // when
        MvcTestResult result = request.exchange();

        // then
        assertThat(result).hasStatus(HttpStatus.UNAUTHORIZED);
        assertThat(result).hasContentType(MediaType.APPLICATION_PROBLEM_JSON);
        assertThat(result).bodyJson().extractingPath("$.code").isEqualTo("common.unauthorized");
    }

    @Test
    @DisplayName("A denied access returns 403")
    void accessDenied_rejected() {
        // given
        MockMvcTester.MockMvcRequestBuilder request = mvc.get().uri("/access-denied");

        // when
        MvcTestResult result = request.exchange();

        // then
        assertThat(result).hasStatus(HttpStatus.FORBIDDEN);
        assertThat(result).hasContentType(MediaType.APPLICATION_PROBLEM_JSON);
        assertThat(result).bodyJson().extractingPath("$.code").isEqualTo("common.forbidden");
    }

    @Test
    @DisplayName("An unexpected exception returns 500 without internals")
    void unexpected_rejected() {
        // given
        MockMvcTester.MockMvcRequestBuilder request = mvc.get().uri("/unexpected");

        // when
        MvcTestResult result = request.exchange();

        // then
        assertThat(result).hasStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(result).hasContentType(MediaType.APPLICATION_PROBLEM_JSON);
        assertThat(result).bodyJson().extractingPath("$.code").isEqualTo("common.internal_server_error");
        assertThat(result).bodyJson().extractingPath("$.detail").isEqualTo("Unexpected server error");
    }

    @RestController
    static class ThrowingController {

        @PostMapping("/validated")
        void validated(@Valid @RequestBody NamedRequest request) {
        }

        @GetMapping("/api-exception")
        void apiException() {
            throw new ApiException(TestErrorCode.CONFLICT);
        }

        @GetMapping("/unauthenticated")
        void unauthenticated() {
            throw new BadCredentialsException("Bad credentials");
        }

        @GetMapping("/access-denied")
        void accessDenied() {
            throw new AccessDeniedException("Access denied");
        }

        @GetMapping("/unexpected")
        void unexpected() {
            throw new IllegalStateException("Database password is wrong");
        }
    }

    record NamedRequest(@NotBlank String name) {
    }

    @Getter
    @Accessors(fluent = true)
    @RequiredArgsConstructor
    enum TestErrorCode implements ErrorCode {

        CONFLICT("test.conflict", HttpStatus.CONFLICT, "Test conflict");

        private final String code;
        private final HttpStatus status;
        private final String messageTemplate;
    }
}
