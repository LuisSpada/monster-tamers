package com.monstertamers.shared.error;

import org.jspecify.annotations.Nullable;
import org.springframework.context.MessageSourceResolvable;

/**
 * One invalid field in a {@code common.validation_failed} response.
 *
 * @param field   request field or parameter name
 * @param code    violated constraint, e.g. {@code NotBlank}
 * @param message human-readable explanation
 */
public record FieldViolation(String field, @Nullable String code, @Nullable String message) {

    static FieldViolation of(String field, MessageSourceResolvable error) {
        String[] codes = error.getCodes();
        // Spring lists codes from most to least specific; the last one is the bare constraint name.
        String code = codes != null && codes.length > 0 ? codes[codes.length - 1] : null;
        return new FieldViolation(field, code, error.getDefaultMessage());
    }
}
