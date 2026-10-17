package com.monstertamers.shared.error;

import org.springframework.http.HttpStatus;

/**
 * Describes one API error: the code the frontend matches on, the HTTP status and the response text.
 */
public interface ErrorCode {

    /** Stable machine-readable code, e.g. {@code monster.not_found}. */
    String code();

    HttpStatus status();

    /** {@link String#format} template for the response {@code detail}. */
    String messageTemplate();
}
