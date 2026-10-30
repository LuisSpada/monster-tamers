package com.monstertamers.auth.error;

import org.springframework.web.ErrorResponseException;

/**
 * The single exception for expected API errors. Becomes an error response built from its {@link ErrorCode}.
 */
public class ApiException extends ErrorResponseException {

    public ApiException(ErrorCode errorCode, Object... args) {
        super(errorCode.status(), ProblemDetailFactory.create(errorCode, args), null);
    }
}
