package com.monstertamers.encounter.internal.integration.simulator;

import com.monstertamers.encounter.internal.domain.Battle;
import com.monstertamers.encounter.internal.domain.BattleLog;
import com.monstertamers.encounter.internal.domain.BattleLogEntry;
import com.monstertamers.encounter.internal.domain.BattleParticipant;
import com.monstertamers.encounter.internal.domain.Encounter;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class SimulatorBattleMapper {

    public Battle toBattle(
            UUID accountId,
            Encounter encounter,
            SimulatorBattleResponse response
    ) {
        Battle battle = new Battle();
        battle.setAccountId(accountId);
        battle.setEncounter(encounter);
        battle.setOutcome(response.outcome());
        battle.setRounds(response.rounds());
        battle.setPlayedAt(Instant.now());

        List<BattleParticipant> participants = response.participants()
            .stream()
            .map(this::toParticipant)
            .toList();

        battle.setParticipants(participants);
        return battle;
    }

    public BattleLog toBattleLog(
            Battle battle,
            SimulatorBattleResponse response
    ) {
        BattleLog battleLog = new BattleLog();
        battleLog.setBattle(battle);

        List<BattleLogEntry> events = response.events()
            .stream()
            .map(this::toLogEntry)
            .toList();

        battleLog.setEvents(events);
        return battleLog;
    }

    private BattleParticipant toParticipant(SimulatorBattleResponse.Participant participant) {

        return BattleParticipant.builder()
                .monsterId(participant.monsterId())
                .name(participant.name())
                .monsterClass(participant.monsterClass())
                .level(participant.level())
                .build();
    }

    private BattleLogEntry toLogEntry(SimulatorBattleResponse.Event event) {

        return BattleLogEntry.builder()
                .id(event.id())
                .round(event.round())
                .side(event.side())
                .kind(event.kind())
                .message(event.message())
                .build();
    }
}
