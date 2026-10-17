package com.monstertamers.shared.error;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.validation.method.ParameterValidationResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import org.springframework.web.util.WebUtils;

/**
 * Turns every exception into a {@link ProblemDetail} with a {@code code}.
 * {@link ApiException} and Spring's own exceptions are handled by {@link ResponseEntityExceptionHandler}.
 */
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(AuthenticationException.class)
    public ProblemDetail handleAuthentication(AuthenticationException ex) {
        return ProblemDetailFactory.create(CommonErrorCode.UNAUTHORIZED);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ProblemDetail handleAccessDenied(AccessDeniedException ex) {
        return ProblemDetailFactory.create(CommonErrorCode.FORBIDDEN);
    }

    /** Anything unexpected: logged with the stack trace, the client gets no internals. */
    @ExceptionHandler(Exception.class)
    public ProblemDetail handleUnexpected(Exception ex, WebRequest request) {
        log.error("Unhandled exception", ex);
        // Lets metrics and tracing see what caused the 500.
        request.setAttribute(WebUtils.ERROR_EXCEPTION_ATTRIBUTE, ex, RequestAttributes.SCOPE_REQUEST);
        return ProblemDetailFactory.create(CommonErrorCode.INTERNAL_SERVER_ERROR);
    }

    /** {@code @Valid @RequestBody} failed. */
    @Override
    protected @Nullable ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request
    ) {
        List<FieldViolation> violations = new ArrayList<>();
        ex.getFieldErrors().forEach(error -> violations.add(FieldViolation.of(error.getField(), error)));
        ex.getGlobalErrors().forEach(error -> violations.add(FieldViolation.of(error.getObjectName(), error)));
        return handleExceptionInternal(ex, validationProblem(violations), headers, status, request);
    }

    /** Constraints on {@code @RequestParam}, {@code @PathVariable} and similar parameters failed. */
    @Override
    protected @Nullable ResponseEntity<Object> handleHandlerMethodValidationException(
            HandlerMethodValidationException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request
    ) {
        List<FieldViolation> violations = new ArrayList<>();
        for (ParameterValidationResult result : ex.getParameterValidationResults()) {
            String parameter = parameterName(result.getMethodParameter());
            for (MessageSourceResolvable error : result.getResolvableErrors()) {
                String field = error instanceof FieldError fieldError ? fieldError.getField() : parameter;
                violations.add(FieldViolation.of(field, error));
            }
        }
        return handleExceptionInternal(ex, validationProblem(violations), headers, status, request);
    }

    @Override
    protected @Nullable ResponseEntity<Object> handleExceptionInternal(
            Exception ex,
            @Nullable Object body,
            HttpHeaders headers,
            HttpStatusCode statusCode,
            WebRequest request
    ) {
        ResponseEntity<Object> response = super.handleExceptionInternal(ex, body, headers, statusCode, request);
        if (response != null && response.getBody() instanceof ProblemDetail problem) {
            addCodeIfMissing(problem);
        }
        return response;
    }

    private static ProblemDetail validationProblem(List<FieldViolation> violations) {
        ProblemDetail problem = ProblemDetailFactory.create(CommonErrorCode.VALIDATION_FAILED);
        problem.setProperty("errors", violations);
        return problem;
    }

    /** Spring builds its own errors (404, 405, malformed JSON) without our code; adds {@code common.<status>}. */
    private static void addCodeIfMissing(ProblemDetail problem) {
        Map<String, Object> properties = problem.getProperties();
        if (properties != null && properties.containsKey("code")) {
            return;
        }
        String status = HttpStatus.valueOf(problem.getStatus()).name().toLowerCase(Locale.ROOT);
        problem.setProperty("code", "common." + status);
    }

    private static String parameterName(MethodParameter parameter) {
        String name = parameter.getParameterName();
        return name != null ? name : "arg" + parameter.getParameterIndex();
    }
}
