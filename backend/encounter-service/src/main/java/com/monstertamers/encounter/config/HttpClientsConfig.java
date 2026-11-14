package com.monstertamers.encounter.config;

import com.monstertamers.encounter.internal.integration.monster.MonsterClient;
import com.monstertamers.encounter.internal.integration.simulator.SimulatorClient;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.service.registry.ImportHttpServices;

@Configuration
@ImportHttpServices(
        group = "monster",
        types = MonsterClient.class
)
@ImportHttpServices(
        group = "simulator",
        types = SimulatorClient.class
)
public class HttpClientsConfig {
}