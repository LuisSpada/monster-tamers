package com.monstertamers.web.error;

import org.springframework.http.ProblemDetail;

/** Builds error responses: a {@link ProblemDetail} with our {@code code}. */
final class ProblemDetailFactory {

    private ProblemDetailFactory() {
    }

    static ProblemDetail create(ErrorCode errorCode, Object... args) {
        String detail = errorCode.messageTemplate().formatted(args);
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(errorCode.status(), detail);
        problem.setProperty("code", errorCode.code());
        return problem;
    }
}
