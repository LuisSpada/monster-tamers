package com.monstertamers.player.api;

import java.util.UUID;

/** Published when a new player profile is created for an account. */
public record PlayerCreatedEvent(UUID accountId) {
}
