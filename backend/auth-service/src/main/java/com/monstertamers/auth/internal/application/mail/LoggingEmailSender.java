package com.monstertamers.auth.internal.application.mail;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/** Temporary: writes emails to the log. Replace with SMTP sending. */
@Slf4j
@Component
public class LoggingEmailSender implements EmailSender {

    @Override
    public void send(String to, String subject, String body) {
        log.info("Email to {}: {}\n{}", to, subject, body);
    }
}
