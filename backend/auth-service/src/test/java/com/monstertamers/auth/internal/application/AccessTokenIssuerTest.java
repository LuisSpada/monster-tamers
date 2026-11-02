package com.monstertamers.auth.internal.application;

import static org.assertj.core.api.Assertions.assertThat;

import com.monstertamers.auth.config.JwtProperties;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.UUID;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

class AccessTokenIssuerTest {

    private static final SecretKey KEY = new SecretKeySpec(
            "test-secret-at-least-32-bytes-long".getBytes(StandardCharsets.UTF_8), "HmacSHA256");
    private static final Duration TTL = Duration.ofMinutes(15);

    private final AccessTokenIssuer issuer = new AccessTokenIssuer(
            NimbusJwtEncoder.withSecretKey(KEY).build(),
            new JwtProperties("unused", TTL)
    );

    @Test
    void token_carriesAccountIdAndExpiry() {
        // given
        UUID accountId = UUID.randomUUID();

        // when
        String token = issuer.issue(accountId);

        // then
        Jwt jwt = NimbusJwtDecoder.withSecretKey(KEY).build().decode(token);
        assertThat(jwt.getSubject()).isEqualTo(accountId.toString());
        assertThat(Duration.between(jwt.getIssuedAt(), jwt.getExpiresAt())).isEqualTo(TTL);
    }
}
