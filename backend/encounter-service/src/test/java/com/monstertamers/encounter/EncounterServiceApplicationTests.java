package com.monstertamers.encounter;

import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
class EncounterServiceApplicationTests {

    @Test
    void contextLoads() {
    }
}
