package com.monstertamers.auth.internal.application;

import com.monstertamers.auth.internal.web.dto.request.RegistrationRequest;
import com.monstertamers.auth.internal.web.dto.response.RegistrationResponse;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class RegistrationService {

    public RegistrationResponse register(RegistrationRequest request) {
        return new RegistrationResponse(UUID.randomUUID());
    }
}
