package com.monstertamers.auth.internal.application.mail;

import com.monstertamers.auth.internal.application.ConfirmationCodeIssuedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Service
@RequiredArgsConstructor
public class MailService {

    private static final String CONFIRMATION_SUBJECT =
            "Confirmation code";

    private final EmailSender emailSender;

    @TransactionalEventListener(
            phase = TransactionPhase.AFTER_COMMIT
    )
    public void sendConfirmationCode(
            ConfirmationCodeIssuedEvent event
    ) {
        String body = """
                Your Monster Tamers confirmation code is: %s

                Registration ID: %s

                If you did not request this registration, you can ignore this email.
                """.formatted(
                event.code(),
                event.registrationId()
        );

        try {
            emailSender.send(
                    event.email(),
                    CONFIRMATION_SUBJECT,
                    body
            );
        } catch (RuntimeException exception) {
            log.error(
                    "Confirmation email delivery failed "
                            + "for recipient {} and "
                            + "registration {}",
                    event.email(),
                    event.registrationId(),
                    exception
            );

            throw exception;
        }
    }
}