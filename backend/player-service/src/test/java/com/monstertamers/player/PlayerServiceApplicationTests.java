package com.monstertamers.player;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@SpringBootTest(properties = {
        "app.jwt.secret=test-secret-at-least-32-bytes-long"
})
@Import(TestcontainersConfiguration.class)
class PlayerServiceApplicationTests {

    @Test
    void contextLoads() {
    }
}