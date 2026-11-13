package com.monstertamers.auth.internal.application.mail;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.mail.Message;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

class SmtpEmailSenderTest {

    private static final String FROM =
            "no-reply@monstertamers.example";

    private static final String RECIPIENT =
            "tamer@example.com";

    @Test
    void sendsPlainTextConfirmationEmail()
            throws Exception {
        JavaMailSender mailSender =
                mock(JavaMailSender.class);

        MimeMessage message =
                new JavaMailSenderImpl()
                        .createMimeMessage();

        when(mailSender.createMimeMessage())
                .thenReturn(message);

        SmtpEmailSender sender =
                new SmtpEmailSender(
                        mailSender,
                        FROM
                );

        sender.send(
                RECIPIENT,
                "Confirmation code",
                "Your confirmation code is 123456."
        );

        ArgumentCaptor<MimeMessage> captor =
                ArgumentCaptor.forClass(
                        MimeMessage.class
                );

        verify(mailSender).send(captor.capture());

        MimeMessage sent = captor.getValue();

        assertThat(sent.getFrom())
                .extracting(Object::toString)
                .containsExactly(FROM);

        assertThat(
                sent.getRecipients(
                        Message.RecipientType.TO
                )
        )
                .extracting(Object::toString)
                .containsExactly(RECIPIENT);

        assertThat(sent.getSubject())
                .isEqualTo("Confirmation code");

        assertThat(sent.getContent().toString())
                .contains(
                        "Your confirmation code "
                                + "is 123456."
                );
    }

    @Test
    void smtpDeliveryFailureIsPropagated() {
        JavaMailSender mailSender =
                mock(JavaMailSender.class);

        MimeMessage message =
                new JavaMailSenderImpl()
                        .createMimeMessage();

        when(mailSender.createMimeMessage())
                .thenReturn(message);

        doThrow(
                new MailSendException(
                        "SMTP unavailable"
                )
        )
                .when(mailSender)
                .send(any(MimeMessage.class));

        SmtpEmailSender sender =
                new SmtpEmailSender(
                        mailSender,
                        FROM
                );

        assertThatThrownBy(() ->
                sender.send(
                        RECIPIENT,
                        "Confirmation code",
                        "Body"
                )
        )
                .isInstanceOf(
                        MailSendException.class
                )
                .hasMessageContaining(
                        "SMTP unavailable"
                );
    }
}