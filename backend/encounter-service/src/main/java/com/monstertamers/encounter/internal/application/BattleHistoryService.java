package com.monstertamers.encounter.internal.application;

import com.monstertamers.encounter.internal.domain.Battle;
import com.monstertamers.encounter.internal.domain.BattleLog;
import com.monstertamers.encounter.internal.domain.Encounter;
import com.monstertamers.encounter.internal.integration.simulator.SimulatorBattleMapper;
import com.monstertamers.encounter.internal.integration.simulator.SimulatorBattleResponse;
import com.monstertamers.encounter.internal.persistence.BattleLogRepository;
import com.monstertamers.encounter.internal.persistence.BattleRepository;
import com.monstertamers.encounter.internal.web.dto.response.BattleSummaryDto;
import java.util.List;
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
    private final BattleDtoMapper battleDtoMapper;

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

    @Transactional(readOnly = true)
    public List<BattleSummaryDto> findAll(UUID accountId) {

        return battleRepository
                .findAllByAccountIdOrderByPlayedAtDesc(accountId)
                .stream()
                .map(battleDtoMapper::toSummaryDto)
                .toList();
    }
}
