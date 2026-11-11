package com.monstertamers.web.security;

import java.util.List;
import org.jspecify.annotations.Nullable;
import org.springframework.boot.context.properties.ConfigurationProperties;

/** Paths a service opens without an access token, on top of health and API docs. */
@ConfigurationProperties("app.security")
public record PublicPathsProperties(List<String> publicPaths) {

    public PublicPathsProperties(@Nullable List<String> publicPaths) {
        this.publicPaths = publicPaths == null ? List.of() : List.copyOf(publicPaths);
    }
}
