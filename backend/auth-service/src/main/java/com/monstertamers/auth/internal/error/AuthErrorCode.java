package com.monstertamers.auth.internal.error;

import com.monstertamers.web.error.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.http.HttpStatus;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor
public enum AuthErrorCode implements ErrorCode {

    EMAIL_TAKEN("auth.email_taken", HttpStatus.CONFLICT, "Email is already registered"),
    CONFIRMATION_CODE_INVALID(
            "auth.confirmation_code_invalid",
            HttpStatus.BAD_REQUEST,
            "Confirmation code is invalid"
    ),
    INVALID_CREDENTIALS(
            "auth.invalid_credentials",
            HttpStatus.UNAUTHORIZED,
            "Email or password is invalid"
    ),
    CONFIRMATION_CODE_EXPIRED(
            "auth.confirmation_code_expired",
            HttpStatus.BAD_REQUEST,
            "Confirmation code has expired"
    ),
    CONFIRMATION_ATTEMPTS_EXCEEDED(
            "auth.confirmation_attempts_exceeded",
            HttpStatus.TOO_MANY_REQUESTS,
            "Too many confirmation attempts"
    );

    private final String code;
    private final HttpStatus status;
    private final String messageTemplate;
}
