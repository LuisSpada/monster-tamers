package com.monstertamers.auth.internal.application.mail;

import com.monstertamers.auth.internal.application.ConfirmationCodeIssuedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionalEventListener;

@Service
@RequiredArgsConstructor
public class MailService {

    private final EmailSender emailSender;

    @TransactionalEventListener
    public void sendConfirmationCode(ConfirmationCodeIssuedEvent event) {
        String subject = "Confirmation code";
        String body = "Your code: %s. Registration id: %s".formatted(event.code(), event.registrationId());
        emailSender.send(event.email(), subject, body);
    }
}
