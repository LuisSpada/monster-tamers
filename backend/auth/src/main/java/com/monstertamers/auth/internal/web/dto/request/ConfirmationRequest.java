package com.monstertamers.auth.internal.web.dto.request;

import jakarta.validation.constraints.NotBlank;

public record ConfirmationRequest(

        @NotBlank
        String code
) {
}
