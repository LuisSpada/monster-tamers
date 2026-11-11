package com.monstertamers.auth.config;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.player")
public record PlayerClientProperties(
        String baseUrl,
        Duration connectTimeout,
        Duration readTimeout
) {
}
