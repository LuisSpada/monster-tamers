package com.monstertamers.encounter.internal.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.when;

import com.monstertamers.encounter.internal.domain.AccountEncounterProgress;
import com.monstertamers.encounter.internal.domain.Encounter;
import com.monstertamers.encounter.internal.domain.EncounterOpponent;
import com.monstertamers.encounter.internal.domain.EncounterStatus;
import com.monstertamers.encounter.internal.persistence.AccountEncounterProgressRepository;
import com.monstertamers.encounter.internal.persistence.EncounterOpponentRepository;
import com.monstertamers.encounter.internal.persistence.EncounterRepository;
import com.monstertamers.encounter.internal.web.dto.response.EncounterDto;
import com.monstertamers.web.error.ApiException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

@ExtendWith(MockitoExtension.class)
class EncounterServiceTest {

    private static final UUID ACCOUNT_ID =
            UUID.fromString(
                    "32923c1a-e469-4014-b957-3e56f88a9a10"
            );

    private static final UUID OTHER_ACCOUNT_ID =
            UUID.fromString(
                    "ad2076e0-e8c6-46d5-b411-a31842f2e40d"
            );

    private static final UUID FIRST_ENCOUNTER_ID =
            UUID.fromString(
                    "00000000-0000-0000-0000-000000000001"
            );

    private static final UUID SECOND_ENCOUNTER_ID =
            UUID.fromString(
                    "00000000-0000-0000-0000-000000000002"
            );

    private static final UUID FIRST_OPPONENT_ID =
            UUID.fromString(
                    "10000000-0000-0000-0000-000000000001"
            );

    @Mock
    private EncounterRepository encounterRepository;

    @Mock
    private EncounterOpponentRepository
            opponentRepository;

    @Mock
    private AccountEncounterProgressRepository
            progressRepository;

    @InjectMocks
    private EncounterService encounterService;

    @Test
    void listReturnsCatalogueInRepositoryOrder() {
        Encounter first = encounter(
                FIRST_ENCOUNTER_ID,
                1,
                "Meadow Scouts"
        );

        Encounter second = encounter(
                SECOND_ENCOUNTER_ID,
                2,
                "Old Quarry"
        );

        EncounterOpponent opponent =
                opponent(
                        FIRST_OPPONENT_ID,
                        FIRST_ENCOUNTER_ID
                );

        when(
                encounterRepository
                        .findAllByOrderByOrderAsc()
        ).thenReturn(List.of(first, second));

        when(
                progressRepository
                        .findAllByAccountId(ACCOUNT_ID)
        ).thenReturn(List.of());

        when(
                opponentRepository
                        .findAllByEncounterIdInOrderByEncounterIdAscIdAsc(
                                List.of(
                                        FIRST_ENCOUNTER_ID,
                                        SECOND_ENCOUNTER_ID
                                )
                        )
        ).thenReturn(List.of(opponent));

        List<EncounterDto> result =
                encounterService.findAll(ACCOUNT_ID);

        assertThat(result)
                .extracting(EncounterDto::name)
                .containsExactly(
                        "Meadow Scouts",
                        "Old Quarry"
                );

        assertThat(result.getFirst().opponents())
                .hasSize(1);

        assertThat(result.getFirst().opponents())
                .first()
                .satisfies(dto -> {
                    assertThat(dto.name())
                            .isEqualTo("Meadow Pup");

                    assertThat(dto.monsterClass())
                            .isEqualTo("Fighter");
                });
    }

    @Test
    void newAccountGetsFirstEncounterAvailable() {
        Encounter first = encounter(
                FIRST_ENCOUNTER_ID,
                1,
                "Meadow Scouts"
        );

        Encounter second = encounter(
                SECOND_ENCOUNTER_ID,
                2,
                "Old Quarry"
        );

        when(
                encounterRepository
                        .findAllByOrderByOrderAsc()
        ).thenReturn(List.of(first, second));

        when(
                progressRepository
                        .findAllByAccountId(ACCOUNT_ID)
        ).thenReturn(List.of());

        when(
                opponentRepository
                        .findAllByEncounterIdInOrderByEncounterIdAscIdAsc(
                                List.of(
                                        FIRST_ENCOUNTER_ID,
                                        SECOND_ENCOUNTER_ID
                                )
                        )
        ).thenReturn(List.of());

        List<EncounterDto> result =
                encounterService.findAll(ACCOUNT_ID);

        assertThat(result)
                .extracting(EncounterDto::status)
                .containsExactly(
                        EncounterStatus.available,
                        EncounterStatus.locked
                );
    }

    @Test
    void listAppliesPersistedAccountProgress() {
        Encounter first = encounter(
                FIRST_ENCOUNTER_ID,
                1,
                "Meadow Scouts"
        );

        Encounter second = encounter(
                SECOND_ENCOUNTER_ID,
                2,
                "Old Quarry"
        );

        AccountEncounterProgress firstProgress =
                progress(
                        ACCOUNT_ID,
                        FIRST_ENCOUNTER_ID,
                        EncounterStatus.cleared
                );

        AccountEncounterProgress secondProgress =
                progress(
                        ACCOUNT_ID,
                        SECOND_ENCOUNTER_ID,
                        EncounterStatus.available
                );

        when(
                encounterRepository
                        .findAllByOrderByOrderAsc()
        ).thenReturn(List.of(first, second));

        when(
                progressRepository
                        .findAllByAccountId(ACCOUNT_ID)
        ).thenReturn(
                List.of(
                        firstProgress,
                        secondProgress
                )
        );

        when(
                opponentRepository
                        .findAllByEncounterIdInOrderByEncounterIdAscIdAsc(
                                List.of(
                                        FIRST_ENCOUNTER_ID,
                                        SECOND_ENCOUNTER_ID
                                )
                        )
        ).thenReturn(List.of());

        List<EncounterDto> result =
                encounterService.findAll(ACCOUNT_ID);

        assertThat(result)
                .extracting(EncounterDto::status)
                .containsExactly(
                        EncounterStatus.cleared,
                        EncounterStatus.available
                );
    }

    @Test
    void progressFromAnotherAccountIsNotUsed() {
        Encounter first = encounter(
                FIRST_ENCOUNTER_ID,
                1,
                "Meadow Scouts"
        );

        when(
                encounterRepository
                        .findAllByOrderByOrderAsc()
        ).thenReturn(List.of(first));

        when(
                progressRepository
                        .findAllByAccountId(ACCOUNT_ID)
        ).thenReturn(List.of());

        when(
                progressRepository
                        .findAllByAccountId(
                                OTHER_ACCOUNT_ID
                        )
        ).thenReturn(
                List.of(
                        progress(
                                OTHER_ACCOUNT_ID,
                                FIRST_ENCOUNTER_ID,
                                EncounterStatus.cleared
                        )
                )
        );

        when(
                opponentRepository
                        .findAllByEncounterIdInOrderByEncounterIdAscIdAsc(
                                List.of(
                                        FIRST_ENCOUNTER_ID
                                )
                        )
        ).thenReturn(List.of());

        List<EncounterDto> accountResult =
                encounterService.findAll(ACCOUNT_ID);

        List<EncounterDto> otherResult =
                encounterService.findAll(
                        OTHER_ACCOUNT_ID
                );

        assertThat(
                accountResult.getFirst().status()
        ).isEqualTo(EncounterStatus.available);

        assertThat(
                otherResult.getFirst().status()
        ).isEqualTo(EncounterStatus.cleared);
    }

    @Test
    void detailReturnsEncounterWithResolvedStatus() {
        Encounter encounter = encounter(
                SECOND_ENCOUNTER_ID,
                2,
                "Old Quarry"
        );

        EncounterOpponent opponent =
                opponent(
                        FIRST_OPPONENT_ID,
                        SECOND_ENCOUNTER_ID
                );

        AccountEncounterProgress progress =
                progress(
                        ACCOUNT_ID,
                        SECOND_ENCOUNTER_ID,
                        EncounterStatus.available
                );

        when(
                encounterRepository.findById(
                        SECOND_ENCOUNTER_ID
                )
        ).thenReturn(Optional.of(encounter));

        when(
                progressRepository
                        .findByAccountIdAndEncounterId(
                                ACCOUNT_ID,
                                SECOND_ENCOUNTER_ID
                        )
        ).thenReturn(Optional.of(progress));

        when(
                opponentRepository
                        .findAllByEncounterIdOrderByIdAsc(
                                SECOND_ENCOUNTER_ID
                        )
        ).thenReturn(List.of(opponent));

        EncounterDto result =
                encounterService.findById(
                        ACCOUNT_ID,
                        SECOND_ENCOUNTER_ID
                );

        assertThat(result.id())
                .isEqualTo(SECOND_ENCOUNTER_ID);

        assertThat(result.name())
                .isEqualTo("Old Quarry");

        assertThat(result.status())
                .isEqualTo(
                        EncounterStatus.available
                );

        assertThat(result.rewardClasses())
                .containsExactly(
                        "Fighter",
                        "Trickster"
                );

        assertThat(result.opponents())
                .hasSize(1);
    }

    @Test
    void unknownEncounterIsRejected() {
        UUID unknownId = UUID.randomUUID();

        when(
                encounterRepository.findById(
                        unknownId
                )
        ).thenReturn(Optional.empty());

        Throwable thrown = catchThrowable(() ->
                encounterService.findById(
                        ACCOUNT_ID,
                        unknownId
                )
        );

        assertThat(thrown)
                .isInstanceOfSatisfying(
                        ApiException.class,
                        exception -> {
                            assertThat(
                                    exception.getStatusCode()
                            ).isEqualTo(
                                    HttpStatus.NOT_FOUND
                            );

                            assertThat(
                                    exception
                                            .getBody()
                                            .getProperties()
                            ).containsEntry(
                                    "code",
                                    "encounter.not_found"
                            );
                        }
                );
    }

    private static Encounter encounter(
            UUID id,
            int order,
            String name
    ) {
        Encounter encounter = new Encounter();

        encounter.setId(id);
        encounter.setOrder(order);
        encounter.setName(name);
        encounter.setDescription(
                name + " description"
        );
        encounter.setEggChance(20);
        encounter.setFinalBoss(false);
        encounter.getRewardClasses().addAll(
                List.of(
                        "Fighter",
                        "Trickster"
                )
        );

        return encounter;
    }

    private static EncounterOpponent opponent(
            UUID id,
            UUID encounterId
    ) {
        EncounterOpponent opponent =
                new EncounterOpponent();

        opponent.setId(id);
        opponent.setEncounterId(encounterId);
        opponent.setName("Meadow Pup");
        opponent.setMonsterClass("Fighter");
        opponent.setLevel(1);
        opponent.setAvatarVariant(0);

        return opponent;
    }

    private static AccountEncounterProgress progress(
            UUID accountId,
            UUID encounterId,
            EncounterStatus status
    ) {
        AccountEncounterProgress progress =
                new AccountEncounterProgress();

        progress.setAccountId(accountId);
        progress.setEncounterId(encounterId);
        progress.setStatus(status);

        return progress;
    }
}