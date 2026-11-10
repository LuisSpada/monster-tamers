package com.monstertamers.auth.internal.application;

import com.monstertamers.auth.error.ApiException;
import com.monstertamers.auth.internal.domain.RegistrationAttempt;
import com.monstertamers.auth.internal.error.AuthErrorCode;
import com.monstertamers.auth.internal.persistence.RegistrationAttemptRepository;
import java.time.Duration;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ConfirmationCodeVerifier {

    private static final Duration CONFIRMATION_CODE_LIFETIME = Duration.ofMinutes(15);
    private static final int MAX_FAILED_ATTEMPTS = 5;

    private final RegistrationAttemptRepository registrationAttemptRepository;
    private final PasswordEncoder passwordEncoder;

    public void verify(RegistrationAttempt attempt, String code) {
        if (Instant.now().isAfter(attempt.getCreatedAt().plus(CONFIRMATION_CODE_LIFETIME))) {
            throw new ApiException(AuthErrorCode.CONFIRMATION_CODE_EXPIRED);
        }

        if (registrationAttemptRepository.consumeAttempt(attempt.getId(), MAX_FAILED_ATTEMPTS) == 0) {
            throw new ApiException(AuthErrorCode.CONFIRMATION_ATTEMPTS_EXCEEDED);
        }

        if (!passwordEncoder.matches(code, attempt.getCode())) {
            throw new ApiException(AuthErrorCode.CONFIRMATION_CODE_INVALID);
        }
    }
}
