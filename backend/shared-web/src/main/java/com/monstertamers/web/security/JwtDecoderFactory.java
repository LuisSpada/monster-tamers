package com.monstertamers.web.security;

import java.nio.charset.StandardCharsets;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

public final class JwtDecoderFactory {

    private static final int MIN_SECRET_BYTES = 32;

    private JwtDecoderFactory() {
    }

    public static JwtDecoder create(JwtProperties properties) {
        String configuredSecret = properties.secret();

        if (configuredSecret == null || configuredSecret.isBlank()) {
            throw new IllegalStateException(
                    "app.jwt.secret is not set: provide JWT_SECRET"
            );
        }

        byte[] secretBytes =
                configuredSecret.getBytes(StandardCharsets.UTF_8);

        if (secretBytes.length < MIN_SECRET_BYTES) {
            throw new IllegalStateException(
                    "app.jwt.secret must be at least "
                            + MIN_SECRET_BYTES
                            + " bytes"
            );
        }

        SecretKey secretKey =
                new SecretKeySpec(secretBytes, "HmacSHA256");

        return NimbusJwtDecoder.withSecretKey(secretKey)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
    }
}
