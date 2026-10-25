package com.monstertamers.auth.internal.application;

import com.monstertamers.auth.internal.domain.RegistrationAttempt;
import com.monstertamers.auth.internal.error.AuthErrorCode;
import com.monstertamers.auth.internal.persistence.AccountRepository;
import com.monstertamers.auth.internal.persistence.RegistrationAttemptRepository;
import com.monstertamers.auth.internal.web.dto.request.RegistrationRequest;
import com.monstertamers.auth.internal.web.dto.response.RegistrationResponse;
import com.monstertamers.shared.error.ApiException;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RegistrationService {

    private final AccountRepository accountRepository;
    private final RegistrationAttemptRepository registrationAttemptRepository;
    private final CodeGenerator codeGenerator;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public RegistrationResponse register(RegistrationRequest request) {
        String email = request.email().trim().toLowerCase(Locale.ROOT);
        if (accountRepository.existsByEmail(email)) {
            throw new ApiException(AuthErrorCode.EMAIL_TAKEN);
        }

        registrationAttemptRepository.deleteAllByEmail(email);

        RegistrationAttempt attempt = new RegistrationAttempt();
        attempt.setEmail(email);
        attempt.setPassword(passwordEncoder.encode(request.password()));
        attempt.setCode(passwordEncoder.encode(codeGenerator.confirmationCode()));
        RegistrationAttempt saved = registrationAttemptRepository.save(attempt);

        return new RegistrationResponse(saved.getId());
    }
}
