package com.monstertamers.web.security;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.time.Instant;
import java.util.UUID;
import javax.crypto.spec.SecretKeySpec;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootTest(
        classes = JwtSecurityAutoConfigurationTest.TestApplication.class,
        properties = {
                "app.jwt.secret=" + JwtSecurityAutoConfigurationTest.SECRET,
                "app.security.public-paths=/open"
        }
)
@AutoConfigureMockMvc
class JwtSecurityAutoConfigurationTest {

    static final String SECRET = "test-secret-at-least-32-bytes-long";

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("A request without a token is rejected")
    void withoutToken_unauthorized() throws Exception {
        mockMvc.perform(get("/private"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.code").value("common.unauthorized"));
    }

    @Test
    @DisplayName("A request with a valid token is let in as the token's account")
    void withToken_succeeds() throws Exception {
        // given
        String accountId = UUID.randomUUID().toString();

        // when
        mockMvc.perform(get("/private").header("Authorization", "Bearer " + token(accountId)))
                // then
                .andExpect(status().isOk())
                .andExpect(content().string(accountId));
    }

    @Test
    @DisplayName("Health is open without a token")
    void health_open() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Internal paths are open without a token")
    void internalPath_open() throws Exception {
        mockMvc.perform(get("/internal/ping"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("A path from app.security.public-paths is open without a token")
    void publicPath_open() throws Exception {
        mockMvc.perform(get("/open"))
                .andExpect(status().isOk());
    }

    private static String token(String subject) {
        var key = new SecretKeySpec(SECRET.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        var claims = JwtClaimsSet.builder()
                .subject(subject)
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(300))
                .build();
        var parameters = JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims);
        return NimbusJwtEncoder.withSecretKey(key).build().encode(parameters).getTokenValue();
    }

    @SpringBootConfiguration
    @EnableAutoConfiguration
    @Import(TestController.class)
    static class TestApplication {
    }

    @RestController
    static class TestController {

        @GetMapping("/private")
        String privatePath(Principal principal) {
            return principal.getName();
        }

        @GetMapping("/open")
        String openPath() {
            return "open";
        }

        @GetMapping("/internal/ping")
        String internalPath() {
            return "internal";
        }

        /** Stands in for actuator, which shared-web does not depend on. */
        @GetMapping("/actuator/health")
        String health() {
            return "UP";
        }
    }
}
