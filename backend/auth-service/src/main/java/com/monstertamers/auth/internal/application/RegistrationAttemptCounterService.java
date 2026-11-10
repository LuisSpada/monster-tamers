package com.monstertamers.auth.internal.application;

import com.monstertamers.auth.internal.domain.RegistrationAttempt;
import com.monstertamers.auth.internal.persistence.RegistrationAttemptRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RegistrationAttemptCounterService {

    private final RegistrationAttemptRepository registrationAttemptRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public int consumeAttempt(RegistrationAttempt attempt, int maxFailedAttempts) {
        return registrationAttemptRepository.consumeAttempt(attempt.getId(), maxFailedAttempts);
    }
}
