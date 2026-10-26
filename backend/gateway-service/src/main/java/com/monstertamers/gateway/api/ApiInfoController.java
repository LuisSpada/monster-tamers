package com.monstertamers.gateway.api;

import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class ApiInfoController {

    @GetMapping
    Map<String, String> info() {
        return Map.of("application", "Monster Tamers Backend", "status", "starting-point");
    }
}
