package com.monstertamers.auth.internal.web.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.Objects;

public record RegistrationRequest(

        @NotBlank
        @Email
        @NormalizedEmail
        String email,

        @NotBlank
        @Size(min = 8, max = 72)
        String password,

        @NotBlank
        String confirmPassword
) {

    @JsonIgnore
    @AssertTrue(message = "must equal password")
    public boolean isConfirmPasswordMatching() {
        return Objects.equals(password, confirmPassword);
    }
}
