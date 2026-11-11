package com.monstertamers.web.security;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class JwtDecoderFactoryTest {

    @Test
    void nullSecretIsRejected() {
        JwtProperties properties = new JwtProperties(null);

        assertThatThrownBy(() -> JwtDecoderFactory.create(properties))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("app.jwt.secret is not set");
    }

    @Test
    void blankSecretIsRejected() {
        JwtProperties properties = new JwtProperties(" ");

        assertThatThrownBy(() -> JwtDecoderFactory.create(properties))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("app.jwt.secret is not set");
    }

    @Test
    void shortSecretIsRejected() {
        JwtProperties properties = new JwtProperties("too-short");

        assertThatThrownBy(() -> JwtDecoderFactory.create(properties))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("at least 32 bytes");
    }

    @Test
    void secretWithAtLeast32BytesIsAccepted() {
        JwtProperties properties = new JwtProperties(
                "test-secret-at-least-32-bytes-long"
        );

        JwtDecoderFactory.create(properties);
    }
}