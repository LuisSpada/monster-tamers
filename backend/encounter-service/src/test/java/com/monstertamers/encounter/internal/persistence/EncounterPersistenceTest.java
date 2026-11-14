package com.monstertamers.encounter.internal.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import com.monstertamers.encounter.TestcontainersConfiguration;
import com.monstertamers.encounter.internal.domain.AccountEncounterProgress;
import com.monstertamers.encounter.internal.domain.EncounterStatus;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
@Transactional
class EncounterPersistenceTest {

    @Autowired
    private EncounterRepository encounterRepository;

    @Autowired
    private EncounterOpponentRepository
            opponentRepository;

    @Autowired
    private AccountEncounterProgressRepository
            progressRepository;

    @Test
    void flywaySeedsFrontendEncounterCatalogue() {
        var encounters =
                encounterRepository
                        .findAllByOrderByOrderAsc();

        assertThat(encounters).hasSize(5);

        var firstEncounter = encounters.getFirst();

        assertThat(firstEncounter.getName())
                .isEqualTo("Meadow Scouts");

        assertThat(firstEncounter.getEggChance())
                .isEqualTo(20);

        assertThat(firstEncounter.getRewardClasses())
                .containsExactly(
                        "Fighter",
                        "Trickster"
                );

        assertThat(
                opponentRepository
                        .findAllByEncounterIdOrderByIdAsc(
                                firstEncounter.getId()
                        )
        )
                .extracting("name")
                .containsExactly(
                        "Meadow Pup",
                        "Field Mouse"
                );
    }

    @Test
    void progressIsStoredPerAccountAndEncounter() {
        UUID accountId = UUID.randomUUID();

        UUID encounterId =
                encounterRepository
                        .findAllByOrderByOrderAsc()
                        .getFirst()
                        .getId();

        AccountEncounterProgress progress =
                new AccountEncounterProgress();

        progress.setAccountId(accountId);
        progress.setEncounterId(encounterId);
        progress.setStatus(
                EncounterStatus.available
        );

        progressRepository.saveAndFlush(progress);

        assertThat(
                progressRepository
                        .findByAccountIdAndEncounterId(
                                accountId,
                                encounterId
                        )
        )
                .get()
                .extracting(
                        AccountEncounterProgress::getStatus
                )
                .isEqualTo(
                        EncounterStatus.available
                );
    }
}