package com.monstertamers.auth.internal.application.mail;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MailService {

    private final EmailSender emailSender;

    public void sendConfirmationCode(String email, UUID registrationId, String code) {
        String subject = "Confirmation code";
        String body = "Your code: %s. Registration id: %s".formatted(code, registrationId);
        emailSender.send(email, subject, body);
    }
}
