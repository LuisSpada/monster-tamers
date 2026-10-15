package com.monstertamers;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.modulith.docs.Documenter;

class ModularityTests {

    private final ApplicationModules modules = ApplicationModules.of(MonsterTamersApplication.class);

    /** Fails if a module touches another module's internals, or on dependency cycles. */
    @Test
    void verifiesModuleBoundaries() {
        modules.verify();
    }

    /** Writes module diagrams to target/spring-modulith-docs. */
    @Test
    void writesDocumentation() {
        new Documenter(modules).writeDocumentation();
    }
}
