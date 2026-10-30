package com.monstertamers.auth.internal.web.controller;

import com.monstertamers.auth.internal.application.RegistrationService;
import com.monstertamers.auth.internal.web.dto.request.ConfirmationRequest;
import com.monstertamers.auth.internal.web.dto.request.RegistrationRequest;
import com.monstertamers.auth.internal.web.dto.response.RegistrationResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Registration")
@RestController
@RequestMapping("/api/registrations")
@RequiredArgsConstructor
public class RegistrationController {

    private final RegistrationService registrationService;

    @PostMapping
    @Operation(summary = "Start registration, sends a confirmation code to the email")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public RegistrationResponse register(@Valid @RequestBody RegistrationRequest request) {
        return registrationService.register(request);
    }

    @PostMapping("/{registrationId}/confirmation")
    @Operation(summary = "Confirm registration with the code, creates the account")
    @ResponseStatus(HttpStatus.CREATED)
    public void confirm(
            @PathVariable UUID registrationId,
            @Valid @RequestBody ConfirmationRequest request
    ) {
        registrationService.confirm(registrationId, request);
    }
}
