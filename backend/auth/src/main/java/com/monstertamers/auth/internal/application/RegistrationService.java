package com.monstertamers.auth.internal.application;

import com.monstertamers.auth.internal.error.AuthErrorCode;
import com.monstertamers.auth.internal.persistence.AccountRepository;
import com.monstertamers.auth.internal.web.dto.request.RegistrationRequest;
import com.monstertamers.auth.internal.web.dto.response.RegistrationResponse;
import com.monstertamers.shared.error.ApiException;
import java.util.Locale;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RegistrationService {

    private final AccountRepository accountRepository;

    public RegistrationResponse register(RegistrationRequest request) {
        String email = request.email().trim().toLowerCase(Locale.ROOT);
        if (accountRepository.existsByEmail(email)) {
            throw new ApiException(AuthErrorCode.EMAIL_TAKEN);
        }
        return new RegistrationResponse(UUID.randomUUID());
    }
}
