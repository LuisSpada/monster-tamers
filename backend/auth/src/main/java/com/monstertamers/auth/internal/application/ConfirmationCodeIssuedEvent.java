package com.monstertamers.auth.internal.application;

import java.util.UUID;

public record ConfirmationCodeIssuedEvent(String email, UUID registrationId, String code) {
}
