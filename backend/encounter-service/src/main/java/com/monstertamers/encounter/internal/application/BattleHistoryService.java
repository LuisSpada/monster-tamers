package com.monstertamers.encounter.internal.application;

import com.monstertamers.encounter.internal.domain.Battle;
import com.monstertamers.encounter.internal.domain.BattleLog;
import com.monstertamers.encounter.internal.domain.BattleLogEntry;
import com.monstertamers.encounter.internal.domain.BattleRewards;
import com.monstertamers.encounter.internal.domain.Encounter;
import com.monstertamers.encounter.internal.error.EncounterErrorCode;
import com.monstertamers.encounter.internal.integration.simulator.SimulatorBattleMapper;
import com.monstertamers.encounter.internal.integration.simulator.SimulatorBattleResponse;
import com.monstertamers.encounter.internal.persistence.BattleLogRepository;
import com.monstertamers.encounter.internal.persistence.BattleRepository;
import com.monstertamers.encounter.internal.web.dto.response.BattleResultDto;
import com.monstertamers.encounter.internal.web.dto.response.BattleSummaryDto;
import com.monstertamers.web.error.ApiException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class BattleHistoryService {

    private final BattleRepository battleRepository;
    private final BattleLogRepository battleLogRepository;
    private final SimulatorBattleMapper simulatorBattleMapper;
    private final BattleDtoMapper battleDtoMapper;

    @Transactional
    public BattleResultDto save(
            UUID accountId,
            Encounter encounter,
            SimulatorBattleResponse response,
            BattleRewards rewards
    ) {

        Battle battle = simulatorBattleMapper.toBattle(accountId, encounter, response);
        battle.setRewards(rewards);
        battleRepository.save(battle);

        BattleLog battleLog = simulatorBattleMapper.toBattleLog(battle, response);
        battleLogRepository.save(battleLog);

        return battleDtoMapper.toResultDto(battle, battleLog.getEvents());
    }

    @Transactional(readOnly = true)
    public List<BattleSummaryDto> findAll(UUID accountId) {

        return battleRepository
                .findAllByAccountIdOrderByPlayedAtDesc(accountId)
                .stream()
                .map(battleDtoMapper::toSummaryDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public BattleResultDto findById(UUID accountId, UUID battleId) {

        Battle battle = battleRepository
                .findByIdAndAccountId(battleId, accountId)
                .orElseThrow(() -> new ApiException(EncounterErrorCode.BATTLE_NOT_FOUND));

        List<BattleLogEntry> events = loadEvents(battle.getId());
        return battleDtoMapper.toResultDto(battle, events);
    }

    private List<BattleLogEntry> loadEvents(UUID battleId) {

        Optional<BattleLog> battleLog = battleLogRepository.findById(battleId);
        if (battleLog.isEmpty()) {
            log.warn("Battle log is missing for battle {}", battleId);
            return List.of();
        }

        return battleLog.get().getEvents();
    }
}
