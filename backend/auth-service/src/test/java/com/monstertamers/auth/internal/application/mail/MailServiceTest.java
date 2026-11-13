package com.monstertamers.auth.internal.application.mail;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import com.monstertamers.auth.internal.application.ConfirmationCodeIssuedEvent;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MailServiceTest {

    private static final String EMAIL =
            "tamer@example.com";

    private static final String CODE =
            "123456";

    private static final UUID REGISTRATION_ID =
            UUID.fromString(
                    "d0775e50-aad9-4fd3-a016-c6efa18df154"
            );

    @Mock
    private EmailSender emailSender;

    @InjectMocks
    private MailService mailService;

    @Test
    void sendsExpectedConfirmationEmail() {
        ConfirmationCodeIssuedEvent event =
                new ConfirmationCodeIssuedEvent(
                        EMAIL,
                        REGISTRATION_ID,
                        CODE
                );

        mailService.sendConfirmationCode(event);

        ArgumentCaptor<String> subject =
                ArgumentCaptor.forClass(String.class);

        ArgumentCaptor<String> body =
                ArgumentCaptor.forClass(String.class);

        verify(emailSender).send(
                org.mockito.ArgumentMatchers.eq(EMAIL),
                subject.capture(),
                body.capture()
        );

        org.assertj.core.api.Assertions
                .assertThat(subject.getValue())
                .isEqualTo("Confirmation code");

        org.assertj.core.api.Assertions
                .assertThat(body.getValue())
                .contains(CODE)
                .contains(REGISTRATION_ID.toString())
                .contains(
                        "If you did not request "
                                + "this registration"
                );
    }

    @Test
    void senderFailureIsPropagated() {
        ConfirmationCodeIssuedEvent event =
                new ConfirmationCodeIssuedEvent(
                        EMAIL,
                        REGISTRATION_ID,
                        CODE
                );

        IllegalStateException failure =
                new IllegalStateException(
                        "SMTP unavailable"
                );

        doThrow(failure)
                .when(emailSender)
                .send(
                        org.mockito.ArgumentMatchers
                                .eq(EMAIL),
                        org.mockito.ArgumentMatchers
                                .anyString(),
                        org.mockito.ArgumentMatchers
                                .anyString()
                );

        assertThatThrownBy(() ->
                mailService.sendConfirmationCode(event)
        )
                .isSameAs(failure);
    }
}