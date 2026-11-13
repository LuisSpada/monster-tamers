package com.monstertamers.player.config;

import com.monstertamers.player.internal.integration.client.monster.MonsterClient;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.service.registry.ImportHttpServices;

@Configuration
@ImportHttpServices(group = "monster", types = MonsterClient.class)
public class HttpClientsConfig {
}
