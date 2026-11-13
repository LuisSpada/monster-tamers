package com.monstertamers.auth.internal.application.mail;

import lombok.extern.slf4j.Slf4j;

/**
 * Local development sender.
 *
 * This implementation reveals the confirmation code and must
 * never be enabled in production.
 */
@Slf4j
public class LoggingEmailSender implements EmailSender {

    @Override
    public void send(
            String to,
            String subject,
            String body
    ) {
        log.info(
                "Development email to {}: {}\n{}",
                to,
                subject,
                body
        );
    }
}