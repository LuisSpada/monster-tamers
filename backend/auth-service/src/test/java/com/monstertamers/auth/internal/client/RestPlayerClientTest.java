package com.monstertamers.auth.internal.client;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

class RestPlayerClientTest {

    @Test
    void createPlayer_postsAccountIdToPlayerService() {
        RestClient.Builder builder = RestClient.builder().baseUrl("http://player-service");
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        RestPlayerClient client = new RestPlayerClient(builder.build());
        UUID accountId = UUID.randomUUID();

        server.expect(requestTo("http://player-service/internal/players"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().json("""
                        {"accountId":"%s"}
                        """.formatted(accountId)))
                .andRespond(withSuccess());

        client.createPlayer(accountId);

        server.verify();
    }

    @Test
    void createPlayer_propagatesPlayerServiceFailure() {
        RestClient.Builder builder = RestClient.builder().baseUrl("http://player-service");
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        RestPlayerClient client = new RestPlayerClient(builder.build());

        server.expect(requestTo("http://player-service/internal/players"))
                .andRespond(withServerError());

        assertThatThrownBy(() -> client.createPlayer(UUID.randomUUID()))
                .isInstanceOf(RuntimeException.class);
    }
}
