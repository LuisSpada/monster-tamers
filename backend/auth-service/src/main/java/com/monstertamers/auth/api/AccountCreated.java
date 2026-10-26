package com.monstertamers.auth.api;

import java.util.UUID;

/** Published whenever an account is created, whatever the way. */
public record AccountCreated(UUID accountId, String email) {
}
