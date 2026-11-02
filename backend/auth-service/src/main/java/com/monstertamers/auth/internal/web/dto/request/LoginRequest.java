package com.monstertamers.auth.internal.web.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(

        @NotBlank
        @Email
        @NormalizedEmail
        String email,

        @NotBlank
        String password
) {
}
