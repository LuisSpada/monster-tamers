package com.monstertamers.auth.config;

import com.monstertamers.auth.internal.application.mail.EmailSender;
import com.monstertamers.auth.internal.application.mail.LoggingEmailSender;
import com.monstertamers.auth.internal.application.mail.SmtpEmailSender;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.util.StringUtils;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(
        MailDeliveryProperties.class
)
public class MailConfiguration {

    @Bean
    @ConditionalOnProperty(
            name = "app.mail.mode",
            havingValue = "logging",
            matchIfMissing = true
    )
    EmailSender loggingEmailSender() {
        return new LoggingEmailSender();
    }

    @Bean
    @ConditionalOnProperty(
            name = "app.mail.mode",
            havingValue = "smtp"
    )
    JavaMailSender smtpMailSender(
            MailDeliveryProperties properties
    ) {
        validateSmtpConfiguration(properties);

        MailDeliveryProperties.Smtp smtp =
                properties.getSmtp();

        JavaMailSenderImpl sender =
                new JavaMailSenderImpl();

        sender.setHost(smtp.getHost());
        sender.setPort(smtp.getPort());
        sender.setDefaultEncoding(
                StandardCharsets.UTF_8.name()
        );

        if (StringUtils.hasText(smtp.getUsername())) {
            sender.setUsername(smtp.getUsername());
        }

        if (StringUtils.hasText(smtp.getPassword())) {
            sender.setPassword(smtp.getPassword());
        }

        Properties javaMailProperties =
                sender.getJavaMailProperties();

        javaMailProperties.put(
                "mail.smtp.auth",
                Boolean.toString(smtp.isAuth())
        );

        javaMailProperties.put(
                "mail.smtp.starttls.enable",
                Boolean.toString(smtp.isStarttls())
        );

        javaMailProperties.put(
                "mail.smtp.connectiontimeout",
                Integer.toString(
                        smtp.getConnectionTimeoutMs()
                )
        );

        javaMailProperties.put(
                "mail.smtp.timeout",
                Integer.toString(
                        smtp.getReadTimeoutMs()
                )
        );

        javaMailProperties.put(
                "mail.smtp.writetimeout",
                Integer.toString(
                        smtp.getWriteTimeoutMs()
                )
        );

        return sender;
    }

    @Bean
    @ConditionalOnProperty(
            name = "app.mail.mode",
            havingValue = "smtp"
    )
    EmailSender smtpEmailSender(
            JavaMailSender mailSender,
            MailDeliveryProperties properties
    ) {
        return new SmtpEmailSender(
                mailSender,
                properties.getFrom()
        );
    }

    private static void validateSmtpConfiguration(
            MailDeliveryProperties properties
    ) {
        MailDeliveryProperties.Smtp smtp =
                properties.getSmtp();

        requireText(
                smtp.getHost(),
                "MAIL_HOST must be set when MAIL_MODE=smtp"
        );

        requireText(
                properties.getFrom(),
                "MAIL_FROM must be set when MAIL_MODE=smtp"
        );

        if (smtp.getPort() < 1
                || smtp.getPort() > 65535) {
            throw new IllegalStateException(
                    "MAIL_PORT must be between 1 and 65535"
            );
        }

        requirePositive(
                smtp.getConnectionTimeoutMs(),
                "MAIL_CONNECTION_TIMEOUT_MS"
        );

        requirePositive(
                smtp.getReadTimeoutMs(),
                "MAIL_READ_TIMEOUT_MS"
        );

        requirePositive(
                smtp.getWriteTimeoutMs(),
                "MAIL_WRITE_TIMEOUT_MS"
        );

        if (smtp.isAuth()) {
            requireText(
                    smtp.getUsername(),
                    "MAIL_USERNAME must be set when "
                            + "MAIL_MODE=smtp and "
                            + "MAIL_SMTP_AUTH=true"
            );

            requireText(
                    smtp.getPassword(),
                    "MAIL_PASSWORD must be set when "
                            + "MAIL_MODE=smtp and "
                            + "MAIL_SMTP_AUTH=true"
            );
        }
    }

    private static void requireText(
            String value,
            String message
    ) {
        if (!StringUtils.hasText(value)) {
            throw new IllegalStateException(message);
        }
    }

    private static void requirePositive(
            int value,
            String propertyName
    ) {
        if (value <= 0) {
            throw new IllegalStateException(
                    propertyName
                            + " must be greater than zero"
            );
        }
    }
}