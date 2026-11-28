package com.monstertamers.encounter.internal.application.battle.rewards;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.monstertamers.encounter.internal.domain.BattleOutcome;
import com.monstertamers.encounter.internal.domain.Egg;
import com.monstertamers.encounter.internal.domain.Encounter;
import com.monstertamers.encounter.internal.integration.monster.MonsterClient;
import com.monstertamers.encounter.internal.integration.monster.request.AddMonsterRequest;
import com.monstertamers.encounter.internal.integration.monster.response.AddedMonsterDto;
import com.monstertamers.encounter.internal.mapper.rewards.EggMapper;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClientException;

@ExtendWith(MockitoExtension.class)
class EggRewardServiceTest {

    private static final UUID ACCOUNT_ID = UUID.fromString("32923c1a-e469-4014-b957-3e56f88a9a10");
    private static final UUID STURDY_CUB_ID = UUID.fromString("44ed4dd1-6abd-4991-97cc-86bf95429a10");

    @InjectMocks
    private EggRewardService eggRewardService;

    @Mock
    private MonsterClient monsterClient;

    @Mock
    private EggMapper eggMapper;

    @Test
    @DisplayName("A defeat gives no egg")
    void grant_defeat_noEgg() {

        // given
        Encounter meadow = encounter(100, List.of("Guardian"));

        // when
        Egg egg = eggRewardService.grant(ACCOUNT_ID, meadow, BattleOutcome.defeat);

        // then
        assertThat(egg).isNull();
        verifyNoInteractions(monsterClient);
    }

    @Test
    @DisplayName("A victory gives no egg when the egg chance is zero")
    void grant_zeroChance_noEgg() {

        // given
        Encounter meadow = encounter(0, List.of("Guardian"));

        // when
        Egg egg = eggRewardService.grant(ACCOUNT_ID, meadow, BattleOutcome.victory);

        // then
        assertThat(egg).isNull();
        verifyNoInteractions(monsterClient);
    }

    @Test
    @DisplayName("A victory gives an egg of the reward class hatched into the collection")
    void grant_fullChance_eggGranted() {

        // given
        Encounter meadow = encounter(100, List.of("Guardian"));

        AddedMonsterDto sturdyCub = new AddedMonsterDto(STURDY_CUB_ID, "Sturdy Cub", "Guardian", 104, 0);
        when(monsterClient.addMonster(ACCOUNT_ID, new AddMonsterRequest("Guardian"))).thenReturn(sturdyCub);

        Egg sturdyCubEgg = new Egg(STURDY_CUB_ID, "Sturdy Cub", "Guardian", 104, 0);
        when(eggMapper.toEgg(sturdyCub)).thenReturn(sturdyCubEgg);

        // when
        Egg egg = eggRewardService.grant(ACCOUNT_ID, meadow, BattleOutcome.victory);

        // then
        assertThat(egg).isSameAs(sturdyCubEgg);
        verify(monsterClient).addMonster(ACCOUNT_ID, new AddMonsterRequest("Guardian"));
    }

    @Test
    @DisplayName("A victory gives no egg when the encounter has no reward classes")
    void grant_noRewardClasses_noEgg() {

        // given
        Encounter meadow = encounter(100, List.of());

        // when
        Egg egg = eggRewardService.grant(ACCOUNT_ID, meadow, BattleOutcome.victory);

        // then
        assertThat(egg).isNull();
        verifyNoInteractions(monsterClient);
    }

    @Test
    @DisplayName("A victory gives no egg when Monster Service is unavailable")
    void grant_monsterServiceDown_noEgg() {

        // given
        Encounter meadow = encounter(100, List.of("Guardian"));

        when(monsterClient.addMonster(any(), any())).thenThrow(new RestClientException("Monster Service is down"));

        // when
        Egg egg = eggRewardService.grant(ACCOUNT_ID, meadow, BattleOutcome.victory);

        // then
        assertThat(egg).isNull();
    }

    private Encounter encounter(int eggChance, List<String> rewardClasses) {

        Encounter encounter = new Encounter();
        encounter.setEggChance(eggChance);
        encounter.setRewardClasses(rewardClasses);

        return encounter;
    }
}
