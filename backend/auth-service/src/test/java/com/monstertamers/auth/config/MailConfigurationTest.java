package com.monstertamers.auth.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.monstertamers.auth.internal.application.mail.EmailSender;
import com.monstertamers.auth.internal.application.mail.LoggingEmailSender;
import com.monstertamers.auth.internal.application.mail.SmtpEmailSender;
import org.junit.jupiter.api.Test;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

class MailConfigurationTest {

    private final MailConfiguration configuration =
            new MailConfiguration();

    @Test
    void loggingSenderIsCreatedForLoggingMode() {
        EmailSender sender =
                configuration.loggingEmailSender();

        assertThat(sender)
                .isInstanceOf(LoggingEmailSender.class);
    }

    @Test
    void smtpConfigurationCreatesMailSender() {
        MailDeliveryProperties properties =
                validSmtpProperties();

        JavaMailSender mailSender =
                configuration.smtpMailSender(properties);

        assertThat(mailSender)
                .isInstanceOf(JavaMailSenderImpl.class);

        JavaMailSenderImpl implementation =
                (JavaMailSenderImpl) mailSender;

        assertThat(implementation.getHost())
                .isEqualTo("smtp.example.com");

        assertThat(implementation.getPort())
                .isEqualTo(587);

        assertThat(implementation.getUsername())
                .isEqualTo("smtp-user");

        assertThat(implementation.getPassword())
                .isEqualTo("smtp-password");

        assertThat(
                implementation
                        .getJavaMailProperties()
                        .getProperty("mail.smtp.auth")
        ).isEqualTo("true");

        assertThat(
                implementation
                        .getJavaMailProperties()
                        .getProperty(
                                "mail.smtp.starttls.enable"
                        )
        ).isEqualTo("true");

        assertThat(
                implementation
                        .getJavaMailProperties()
                        .getProperty(
                                "mail.smtp.connectiontimeout"
                        )
        ).isEqualTo("5000");

        assertThat(
                implementation
                        .getJavaMailProperties()
                        .getProperty("mail.smtp.timeout")
        ).isEqualTo("5000");

        assertThat(
                implementation
                        .getJavaMailProperties()
                        .getProperty(
                                "mail.smtp.writetimeout"
                        )
        ).isEqualTo("5000");
    }

    @Test
    void smtpEmailSenderUsesConfiguredFromAddress() {
        MailDeliveryProperties properties =
                validSmtpProperties();

        JavaMailSender mailSender =
                configuration.smtpMailSender(properties);

        EmailSender emailSender =
                configuration.smtpEmailSender(
                        mailSender,
                        properties
                );

        assertThat(emailSender)
                .isInstanceOf(SmtpEmailSender.class);
    }

    @Test
    void missingHostIsRejected() {
        MailDeliveryProperties properties =
                validSmtpProperties();

        properties.getSmtp().setHost("");

        assertThatThrownBy(() ->
                configuration.smtpMailSender(properties)
        )
                .isInstanceOf(
                        IllegalStateException.class
                )
                .hasMessage(
                        "MAIL_HOST must be set when "
                                + "MAIL_MODE=smtp"
                );
    }

    @Test
    void missingFromAddressIsRejected() {
        MailDeliveryProperties properties =
                validSmtpProperties();

        properties.setFrom("");

        assertThatThrownBy(() ->
                configuration.smtpMailSender(properties)
        )
                .isInstanceOf(
                        IllegalStateException.class
                )
                .hasMessage(
                        "MAIL_FROM must be set when "
                                + "MAIL_MODE=smtp"
                );
    }

    @Test
    void invalidPortIsRejected() {
        MailDeliveryProperties properties =
                validSmtpProperties();

        properties.getSmtp().setPort(0);

        assertThatThrownBy(() ->
                configuration.smtpMailSender(properties)
        )
                .isInstanceOf(
                        IllegalStateException.class
                )
                .hasMessage(
                        "MAIL_PORT must be between "
                                + "1 and 65535"
                );
    }

    @Test
    void authenticatedSmtpRequiresUsername() {
        MailDeliveryProperties properties =
                validSmtpProperties();

        properties.getSmtp().setUsername("");

        assertThatThrownBy(() ->
                configuration.smtpMailSender(properties)
        )
                .isInstanceOf(
                        IllegalStateException.class
                )
                .hasMessageContaining("MAIL_USERNAME")
                .hasMessageContaining(
                        "MAIL_SMTP_AUTH=true"
                );
    }

    @Test
    void authenticatedSmtpRequiresPassword() {
        MailDeliveryProperties properties =
                validSmtpProperties();

        properties.getSmtp().setPassword("");

        assertThatThrownBy(() ->
                configuration.smtpMailSender(properties)
        )
                .isInstanceOf(
                        IllegalStateException.class
                )
                .hasMessageContaining("MAIL_PASSWORD")
                .hasMessageContaining(
                        "MAIL_SMTP_AUTH=true"
                );
    }

    @Test
    void unauthenticatedSmtpPermitsEmptyCredentials() {
        MailDeliveryProperties properties =
                validSmtpProperties();

        properties.getSmtp().setAuth(false);
        properties.getSmtp().setUsername("");
        properties.getSmtp().setPassword("");

        JavaMailSender mailSender =
                configuration.smtpMailSender(properties);

        assertThat(mailSender).isNotNull();
    }

    @Test
    void nonPositiveTimeoutIsRejected() {
        MailDeliveryProperties properties =
                validSmtpProperties();

        properties
                .getSmtp()
                .setConnectionTimeoutMs(0);

        assertThatThrownBy(() ->
                configuration.smtpMailSender(properties)
        )
                .isInstanceOf(
                        IllegalStateException.class
                )
                .hasMessage(
                        "MAIL_CONNECTION_TIMEOUT_MS "
                                + "must be greater than zero"
                );
    }

    private static MailDeliveryProperties
    validSmtpProperties() {
        MailDeliveryProperties properties =
                new MailDeliveryProperties();

        properties.setMode(
                MailDeliveryProperties.Mode.SMTP
        );

        properties.setFrom(
                "no-reply@monstertamers.example"
        );

        properties
                .getSmtp()
                .setHost("smtp.example.com");

        properties
                .getSmtp()
                .setPort(587);

        properties
                .getSmtp()
                .setUsername("smtp-user");

        properties
                .getSmtp()
                .setPassword("smtp-password");

        properties
                .getSmtp()
                .setAuth(true);

        properties
                .getSmtp()
                .setStarttls(true);

        return properties;
    }
}