package com.monstertamers.encounter.internal.integration.monster;

import com.monstertamers.encounter.internal.integration.monster.request.AddMonsterRequest;
import com.monstertamers.encounter.internal.integration.monster.response.AddedMonsterDto;
import java.util.List;
import java.util.UUID;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

@HttpExchange("/internal/accounts")
public interface MonsterClient {

    @PostExchange(
            "/{accountId}/battle-monsters"
    )
    List<BattleMonsterDto> loadBattleMonsters(
            @PathVariable
            UUID accountId,

            @RequestBody
            BattleMonsterRequest request
    );

    @PostExchange("/{accountId}/collection/monsters")
    AddedMonsterDto addMonster(
        @PathVariable UUID accountId,
        @RequestBody AddMonsterRequest request
    );
}
