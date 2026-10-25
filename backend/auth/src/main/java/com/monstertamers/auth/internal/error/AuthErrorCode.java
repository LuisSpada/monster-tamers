package com.monstertamers.auth.internal.error;

import com.monstertamers.shared.error.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.http.HttpStatus;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor
public enum AuthErrorCode implements ErrorCode {

    EMAIL_TAKEN("auth.email_taken", HttpStatus.CONFLICT, "Email is already registered");

    private final String code;
    private final HttpStatus status;
    private final String messageTemplate;
}
