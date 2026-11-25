package com.monstertamers.encounter.internal.application.battle.effects;

import com.monstertamers.encounter.internal.application.EncounterProgressService;
import com.monstertamers.encounter.internal.application.battle.BattleFinishedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AdvanceEncounterProgressOnVictory {

    private final EncounterProgressService progressService;

    @EventListener
    public void handle(BattleFinishedEvent event) {

        if (!event.isVictory()) {
            return;
        }

        progressService.advance(event.accountId(), event.encounterId());
    }
}
