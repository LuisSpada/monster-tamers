package com.monstertamers.player.config;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.monstertamers.player.PlayerServiceApplication;
import com.monstertamers.player.TestcontainersConfiguration;
import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.time.Instant;
import java.util.UUID;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootTest(
        classes = PlayerServiceApplication.class,
        properties = {
                "app.jwt.secret=test-secret-at-least-32-bytes-long"
        }
)
@AutoConfigureMockMvc
@Import({PlayerSecurityIntegrationTest.TestController.class, TestcontainersConfiguration.class})
class PlayerSecurityIntegrationTest {

    private static final String SECRET =
            "test-secret-at-least-32-bytes-long";

    private static final String OTHER_SECRET =
            "different-test-secret-at-least-32-bytes-long";

    @Autowired
    private MockMvc mockMvc;

    @Test
    void validTokenExposesSubjectAsPrincipalName() throws Exception {
        UUID accountId = UUID.randomUUID();

        String token = token(
                SECRET,
                accountId.toString(),
                Instant.now().minusSeconds(10),
                Instant.now().plusSeconds(300)
        );

        mockMvc.perform(get("/test/principal")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(content().string(accountId.toString()));
    }

    @Test
    void missingTokenReturnsProblemDetails() throws Exception {
        assertUnauthorized(
                mockMvc.perform(get("/test/principal"))
        );
    }

    @Test
    void malformedTokenReturnsProblemDetails() throws Exception {
        assertUnauthorized(
                mockMvc.perform(get("/test/principal")
                        .header(
                                "Authorization",
                                "Bearer not-a-valid-jwt"
                        ))
        );
    }

    @Test
    void expiredTokenReturnsProblemDetails() throws Exception {
        String token = token(
                SECRET,
                UUID.randomUUID().toString(),
                Instant.now().minusSeconds(300),
                Instant.now().minusSeconds(120)
        );

        assertUnauthorized(
                mockMvc.perform(get("/test/principal")
                        .header("Authorization", "Bearer " + token))
        );
    }

    @Test
    void incorrectlySignedTokenReturnsProblemDetails() throws Exception {
        String token = token(
                OTHER_SECRET,
                UUID.randomUUID().toString(),
                Instant.now().minusSeconds(10),
                Instant.now().plusSeconds(300)
        );

        assertUnauthorized(
                mockMvc.perform(get("/test/principal")
                        .header("Authorization", "Bearer " + token))
        );
    }

    @Test
    void healthEndpointRemainsPublic() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk());
    }

    private static void assertUnauthorized(
            ResultActions result
    ) throws Exception {
        result
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_PROBLEM_JSON
                ))
                .andExpect(jsonPath("$.code")
                        .value("common.unauthorized"));
    }

    private static String token(
            String secret,
            String subject,
            Instant issuedAt,
            Instant expiresAt
    ) {
        JwtEncoder encoder = NimbusJwtEncoder
                .withSecretKey(secretKey(secret))
                .build();

        JwsHeader header = JwsHeader
                .with(MacAlgorithm.HS256)
                .build();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .subject(subject)
                .issuedAt(issuedAt)
                .expiresAt(expiresAt)
                .build();

        return encoder.encode(
                JwtEncoderParameters.from(header, claims)
        ).getTokenValue();
    }

    private static SecretKey secretKey(String secret) {
        return new SecretKeySpec(
                secret.getBytes(StandardCharsets.UTF_8),
                "HmacSHA256"
        );
    }

    @RestController
    static class TestController {

        @GetMapping("/test/principal")
        String principal(Principal principal) {
            return principal.getName();
        }
    }
}