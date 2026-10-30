package com.monstertamers.auth.internal.web.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record ConfirmationRequest(

        @NotBlank
        @Pattern(regexp = "\\d{6}")
        String code
) {
}
