package com.monstertamers.gateway;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(
        classes = GatewayServiceApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class GatewayRoutesTests {

    @Autowired
    private RouteLocator routeLocator;

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    void routesExposeConfiguredServiceTargets() {
        Map<String, Route> routes = routeLocator.getRoutes()
                .collect(Collectors.toMap(Route::getId, route -> route))
                .block();

        assertThat(routes).containsKeys("auth-service", "player-service", "monster-service", "encounter-service");
        assertThat(routes.get("auth-service").getUri().toString()).isEqualTo("https://auth-service:8081");
        assertThat(routes.get("player-service").getUri().toString()).isEqualTo("https://player-service:8082");
        assertThat(routes.get("monster-service").getUri().toString()).isEqualTo("https://monster-service:8083");
        assertThat(routes.get("encounter-service").getUri().toString()).isEqualTo("https://encounter-service:8084");
    }

    @Test
    void authRouteKeepsPath() {
        Route route = routeLocator.getRoutes()
                .filter(candidate -> candidate.getId().equals("auth-service"))
                .blockFirst();

        assertThat(route).isNotNull();
        assertThat(route.getFilters()).isEmpty();
    }

    @ParameterizedTest
    @CsvSource({
            "auth-service-docs,      https://auth-service:8081",
            "player-service-docs,    https://player-service:8082",
            "monster-service-docs,   https://monster-service:8083",
            "encounter-service-docs, https://encounter-service:8084"
    })
    void docsRouteTargetsServiceDocs(String routeId, String uri) {
        Route route = routeLocator.getRoutes()
                .filter(candidate -> candidate.getId().equals(routeId))
                .blockFirst();

        assertThat(route).isNotNull();
        assertThat(route.getUri().toString()).isEqualTo(uri);
        assertThat(route.getFilters()).anyMatch(filter -> filter.toString().contains("/v3/api-docs"));
    }

    @Test
    void simulatorHasNoPublicRoute() {
        assertThat(routeLocator.getRoutes().map(Route::getId).collectList().block())
                .doesNotContain("simulator-service");
    }

    @Test
    void unknownApiRoutesHaveNoMatchingRoute() {
        WebTestClient.bindToApplicationContext(applicationContext)
                .build()
                .get()
                .uri("/api/unknown")
                .exchange()
                .expectStatus()
                .isNotFound();
    }

    @Test
    void internalRoutesHaveNoMatchingRoute() {
        WebTestClient.bindToApplicationContext(applicationContext)
                .build()
                .get()
                .uri("/internal/players")
                .exchange()
                .expectStatus()
                .isNotFound();
    }
}
