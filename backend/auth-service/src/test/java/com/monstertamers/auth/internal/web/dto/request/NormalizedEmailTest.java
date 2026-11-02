package com.monstertamers.auth.internal.web.dto.request;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

class NormalizedEmailTest {

    private final JsonMapper mapper = JsonMapper.builder().build();

    @Test
    void mixedCaseWithSpaces_normalized() {
        // given
        String json = """
                {"email": "  Tamer@Example.COM ", "password": "secret123"}
                """;

        // when
        LoginRequest request = mapper.readValue(json, LoginRequest.class);

        // then
        assertThat(request.email()).isEqualTo("tamer@example.com");
    }

    @Test
    void nullEmail_staysNull() {
        // given
        String json = """
                {"email": null, "password": "secret123"}
                """;

        // when
        LoginRequest request = mapper.readValue(json, LoginRequest.class);

        // then
        assertThat(request.email()).isNull();
    }
}
