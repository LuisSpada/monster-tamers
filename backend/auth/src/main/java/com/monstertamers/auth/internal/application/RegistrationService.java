package com.monstertamers.auth.internal.application;

import com.monstertamers.auth.internal.application.mail.MailService;
import com.monstertamers.auth.internal.domain.RegistrationAttempt;
import com.monstertamers.auth.internal.error.AuthErrorCode;
import com.monstertamers.auth.internal.persistence.AccountRepository;
import com.monstertamers.auth.internal.persistence.RegistrationAttemptRepository;
import com.monstertamers.auth.internal.web.dto.request.ConfirmationRequest;
import com.monstertamers.auth.internal.web.dto.request.RegistrationRequest;
import com.monstertamers.auth.internal.web.dto.response.RegistrationResponse;
import com.monstertamers.shared.error.ApiException;
import java.util.Locale;
import java.util.UUID;
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
    private final MailService mailService;
    private final AccountService accountService;

    @Transactional
    public RegistrationResponse register(RegistrationRequest request) {
        String email = request.email().trim().toLowerCase(Locale.ROOT);
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

        mailService.sendConfirmationCode(email, saved.getId(), code);

        return new RegistrationResponse(saved.getId());
    }

    @Transactional
    public void confirm(UUID registrationId, ConfirmationRequest request) {

        RegistrationAttempt attempt = registrationAttemptRepository.findById(registrationId)
                .orElseThrow(() -> new ApiException(AuthErrorCode.CONFIRMATION_CODE_INVALID));

        if (!passwordEncoder.matches(request.code(), attempt.getCode())) {
            throw new ApiException(AuthErrorCode.CONFIRMATION_CODE_INVALID);
        }

        if (accountRepository.existsByEmail(attempt.getEmail())) {
            throw new ApiException(AuthErrorCode.EMAIL_TAKEN);
        }

        accountService.create(attempt.getEmail(), attempt.getPassword());
        registrationAttemptRepository.delete(attempt);
    }
}
