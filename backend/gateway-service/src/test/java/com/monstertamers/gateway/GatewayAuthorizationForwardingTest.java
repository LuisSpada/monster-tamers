package com.monstertamers.gateway;

import static org.assertj.core.api.Assertions.assertThat;

import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(
        classes = GatewayServiceApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
class GatewayAuthorizationForwardingTest {

    private static final AtomicReference<String> RECEIVED_AUTHORIZATION =
            new AtomicReference<>();

    private static final HttpServer DOWNSTREAM_SERVER =
            createDownstreamServer();

    @Value("${local.server.port}")
    private int gatewayPort;

    @DynamicPropertySource
    static void gatewayProperties(
            DynamicPropertyRegistry registry
    ) {
        registry.add(
                "PLAYER_SERVICE_URL",
                () -> "http://127.0.0.1:"
                        + DOWNSTREAM_SERVER.getAddress().getPort()
        );
    }

    @AfterAll
    static void stopDownstreamServer() {
        DOWNSTREAM_SERVER.stop(0);
    }

    @Test
    void authorizationHeaderIsForwardedUnchanged() {
        RECEIVED_AUTHORIZATION.set(null);

        String authorization = "Bearer forwarded-test-token";

        WebTestClient webTestClient = WebTestClient.bindToServer()
                .baseUrl("http://127.0.0.1:" + gatewayPort)
                .build();

        webTestClient.get()
                .uri("/api/players/header-probe")
                .header("Authorization", authorization)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(String.class)
                .isEqualTo("ok");

        assertThat(RECEIVED_AUTHORIZATION.get())
                .isEqualTo(authorization);
    }

    private static HttpServer createDownstreamServer() {
        try {
            HttpServer server = HttpServer.create(
                    new InetSocketAddress("127.0.0.1", 0),
                    0
            );

            server.createContext(
                    "/api/players/header-probe",
                    exchange -> {
                        RECEIVED_AUTHORIZATION.set(
                                exchange.getRequestHeaders()
                                        .getFirst("Authorization")
                        );

                        byte[] response =
                                "ok".getBytes(StandardCharsets.UTF_8);

                        exchange.sendResponseHeaders(
                                200,
                                response.length
                        );

                        try (var responseBody =
                                     exchange.getResponseBody()) {
                            responseBody.write(response);
                        }
                    }
            );

            server.start();
            return server;
        } catch (IOException exception) {
            throw new ExceptionInInitializerError(exception);
        }
    }
}