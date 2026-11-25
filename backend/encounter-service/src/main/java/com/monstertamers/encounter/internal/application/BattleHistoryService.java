package com.monstertamers.encounter.internal.application;

import com.monstertamers.encounter.internal.domain.Battle;
import com.monstertamers.encounter.internal.domain.BattleLog;
import com.monstertamers.encounter.internal.domain.Encounter;
import com.monstertamers.encounter.internal.integration.simulator.SimulatorBattleMapper;
import com.monstertamers.encounter.internal.integration.simulator.SimulatorBattleResponse;
import com.monstertamers.encounter.internal.persistence.BattleLogRepository;
import com.monstertamers.encounter.internal.persistence.BattleRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BattleHistoryService {

    private final BattleRepository battleRepository;
    private final BattleLogRepository battleLogRepository;
    private final SimulatorBattleMapper simulatorBattleMapper;

    @Transactional
    public Battle save(
            UUID accountId,
            Encounter encounter,
            SimulatorBattleResponse response
    ) {

        Battle battle = simulatorBattleMapper.toBattle(accountId, encounter, response);
        battleRepository.save(battle);

        BattleLog battleLog = simulatorBattleMapper.toBattleLog(battle, response);
        battleLogRepository.save(battleLog);

        return battle;
    }
}
