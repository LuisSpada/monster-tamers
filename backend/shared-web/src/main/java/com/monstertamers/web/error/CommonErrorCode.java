package com.monstertamers.web.error;

import org.springframework.http.HttpStatus;

/**
 * Errors not tied to any game module: validation, authentication, unexpected failures.
 * Spring's own errors (404, 405, malformed JSON, ...) get {@code common.<http_status>}, e.g. {@code common.not_found}.
 */
public enum CommonErrorCode implements ErrorCode {

    VALIDATION_FAILED("common.validation_failed", HttpStatus.BAD_REQUEST, "Request has invalid fields"),
    UNAUTHORIZED("common.unauthorized", HttpStatus.UNAUTHORIZED, "Authentication is required"),
    FORBIDDEN("common.forbidden", HttpStatus.FORBIDDEN, "Access denied"),
    INTERNAL_SERVER_ERROR("common.internal_server_error", HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected server error");

    private final String code;
    private final HttpStatus status;
    private final String messageTemplate;

    CommonErrorCode(String code, HttpStatus status, String messageTemplate) {
        this.code = code;
        this.status = status;
        this.messageTemplate = messageTemplate;
    }

    @Override
    public String code() {
        return code;
    }

    @Override
    public HttpStatus status() {
        return status;
    }

    @Override
    public String messageTemplate() {
        return messageTemplate;
    }
}
