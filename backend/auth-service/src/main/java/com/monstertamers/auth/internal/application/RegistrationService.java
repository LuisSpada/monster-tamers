package com.monstertamers.auth.internal.application;

import com.monstertamers.auth.internal.domain.RegistrationAttempt;
import com.monstertamers.auth.internal.error.AuthErrorCode;
import com.monstertamers.auth.internal.persistence.AccountRepository;
import com.monstertamers.auth.internal.persistence.RegistrationAttemptRepository;
import com.monstertamers.auth.internal.web.dto.request.ConfirmationRequest;
import com.monstertamers.auth.internal.web.dto.request.RegistrationRequest;
import com.monstertamers.auth.internal.web.dto.response.RegistrationResponse;
import com.monstertamers.auth.error.ApiException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
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
    private final ApplicationEventPublisher eventPublisher;
    private final AccountService accountService;
    private final ConfirmationCodeVerifier confirmationCodeVerifier;

    @Transactional
    public RegistrationResponse register(RegistrationRequest request) {
        String email = request.email();
        if (accountRepository.existsByEmail(email)) {
            throw new ApiException(AuthErrorCode.EMAIL_TAKEN);
        }

        registrationAttemptRepository.deleteAllByEmail(email);

        String code = codeGenerator.confirmationCode();
        RegistrationAttempt attempt = new RegistrationAttempt();
        attempt.setEmail(email);
        attempt.setPassword(passwordEncoder.encode(request.password()));
        attempt.setCode(passwordEncoder.encode(code));
        RegistrationAttempt saved = registrationAttemptRepository.save(attempt);

        eventPublisher.publishEvent(new ConfirmationCodeIssuedEvent(email, saved.getId(), code));

        return new RegistrationResponse(saved.getId());
    }

    @Transactional
    public void confirm(UUID registrationId, ConfirmationRequest request) {

        RegistrationAttempt attempt = registrationAttemptRepository.findById(registrationId)
                .orElseThrow(() -> new ApiException(AuthErrorCode.CONFIRMATION_CODE_INVALID));
        
        confirmationCodeVerifier.verify(attempt, request.code());

        if (accountRepository.existsByEmail(attempt.getEmail())) {
            throw new ApiException(AuthErrorCode.EMAIL_TAKEN);
        }

        accountService.create(attempt.getEmail(), attempt.getPassword());
        registrationAttemptRepository.delete(attempt);
    }
}
