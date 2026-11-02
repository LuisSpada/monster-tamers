package com.monstertamers.auth.internal.application;

import com.monstertamers.auth.internal.web.dto.request.LoginRequest;
import com.monstertamers.auth.internal.web.dto.response.LoginResponse;
import org.springframework.stereotype.Service;

@Service
public class LoginService {

    // Placeholder until refresh tokens are implemented in GH-21.
    private static final String REFRESH_TOKEN_STUB = "implement-me";

    public LoginResponse login(LoginRequest request) {
        return new LoginResponse("implement-me", REFRESH_TOKEN_STUB);
    }
}
