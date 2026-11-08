package com.monstertamers.auth.internal.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import com.monstertamers.auth.error.ApiException;
import com.monstertamers.auth.internal.domain.RegistrationAttempt;
import com.monstertamers.auth.internal.persistence.RegistrationAttemptRepository;
import com.monstertamers.auth.internal.web.dto.request.ConfirmationRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.jdbc.Sql;

@SpringBootTest
@Sql(statements = "DROP TABLE IF EXISTS registration_attempt",
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/registration-attempt-schema.sql",
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(
        statements = "DROP TABLE IF EXISTS registration_attempt",
        executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD
)
class RegistrationServiceIntegrationTest {

    @Autowired
    private RegistrationService registrationService;

    @Autowired
    private RegistrationAttemptRepository registrationAttemptRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void wrongCode_failedAttemptIsPersistedDespiteErrorResponse() {
        RegistrationAttempt attempt = new RegistrationAttempt();
        attempt.setEmail("tamer@example.com");
        attempt.setPassword("password-hash");
        attempt.setCode(passwordEncoder.encode("123456"));
        attempt.setFailedAttempts(0);

        RegistrationAttempt savedAttempt =
                registrationAttemptRepository.saveAndFlush(attempt);

        Throwable thrown = catchThrowable(() ->
                registrationService.confirm(
                        savedAttempt.getId(),
                        new ConfirmationRequest("000000")));

        assertThat(thrown).isInstanceOf(ApiException.class);

        RegistrationAttempt reloaded = registrationAttemptRepository
                .findById(savedAttempt.getId())
                .orElseThrow();

        assertThat(reloaded.getFailedAttempts()).isEqualTo(1);
    }
}
