package com.monstertamers.auth.internal.application;

import com.monstertamers.auth.error.ApiException;
import com.monstertamers.auth.internal.error.AuthErrorCode;
import com.monstertamers.auth.internal.persistence.AccountRepository;
import com.monstertamers.auth.internal.web.dto.request.LoginRequest;
import com.monstertamers.auth.internal.web.dto.response.LoginResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LoginService {

    // Placeholder until refresh tokens are implemented in GH-21.
    private static final String REFRESH_TOKEN_STUB = "implement-me";

    private final AccountRepository accountRepository;

    public LoginResponse login(LoginRequest request) {
        accountRepository.findByEmail(request.email())
                .orElseThrow(() -> new ApiException(AuthErrorCode.INVALID_CREDENTIALS));

        return new LoginResponse("implement-me", REFRESH_TOKEN_STUB);
    }
}
