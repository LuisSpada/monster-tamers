package com.monstertamers.encounter.internal.integration.simulator;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

@HttpExchange("/internal/battles")
public interface SimulatorClient {

    @PostExchange
    SimulatorBattleResponse simulate(
            @RequestBody
            SimulatorBattleRequest request
    );
}