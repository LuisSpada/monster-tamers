package com.monstertamers.player;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
        "app.jwt.secret=test-secret-at-least-32-bytes-long"
})
class PlayerServiceApplicationTests {

    @Test
    void contextLoads() {
    }
}