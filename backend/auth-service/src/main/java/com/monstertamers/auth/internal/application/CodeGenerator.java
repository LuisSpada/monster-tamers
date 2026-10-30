package com.monstertamers.auth.internal.application;

import java.security.SecureRandom;
import org.springframework.stereotype.Component;

@Component
public class CodeGenerator {

    private final SecureRandom random = new SecureRandom();

    public String confirmationCode() {
        return "%06d".formatted(random.nextInt(1_000_000));
    }
}
