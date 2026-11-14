package com.monstertamers.simulator.internal.battle;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class BattleServiceTest {

    private static final UUID ENCOUNTER_ID =
            UUID.fromString(
                    "00000000-0000-0000-0000-000000000001"
            );

    private static final UUID FIRST_MONSTER_ID =
            UUID.fromString(
                    "10000000-0000-0000-0000-000000000001"
            );

    private static final UUID SECOND_MONSTER_ID =
            UUID.fromString(
                    "10000000-0000-0000-0000-000000000002"
            );

    private static final UUID OPPONENT_ID =
            UUID.fromString(
                    "20000000-0000-0000-0000-000000000001"
            );

    private final BattleService battleService =
            new BattleService();

    @Test
    void identicalInputProducesIdenticalResult() {
        BattleRequest request = request(
                List.of(
                        monster(
                                FIRST_MONSTER_ID,
                                "Fierce Fox"
                        ),
                        monster(
                                SECOND_MONSTER_ID,
                                "Stone Bear"
                        )
                )
        );

        BattleResponse first =
                battleService.simulate(request);

        BattleResponse second =
                battleService.simulate(request);

        assertThat(second).isEqualTo(first);
    }

    @Test
    void preservesSubmittedTeamOrder() {
        BattleRequest request = request(
                List.of(
                        monster(
                                SECOND_MONSTER_ID,
                                "Stone Bear"
                        ),
                        monster(
                                FIRST_MONSTER_ID,
                                "Fierce Fox"
                        )
                )
        );

        BattleResponse response =
                battleService.simulate(request);

        assertThat(
                response.participants()
                        .stream()
                        .map(BattleParticipant::monsterId)
        ).containsExactly(
                SECOND_MONSTER_ID,
                FIRST_MONSTER_ID
        );
    }

    @Test
    void producesBattleEventsAndOutcome() {
        BattleResponse response =
                battleService.simulate(
                        request(
                                List.of(
                                        monster(
                                                FIRST_MONSTER_ID,
                                                "Fierce Fox"
                                        )
                                )
                        )
                );

        assertThat(response.id()).isNotNull();
        assertThat(response.encounterId())
                .isEqualTo(ENCOUNTER_ID);

        assertThat(response.outcome())
                .isIn("victory", "defeat");

        assertThat(response.rounds())
                .isBetween(1, 10);

        assertThat(response.events())
                .isNotEmpty();

        assertThat(response.events())
                .allSatisfy(event -> {
                    assertThat(event.id())
                            .isNotBlank();

                    assertThat(event.round())
                            .isBetween(
                                    1,
                                    response.rounds()
                            );

                    assertThat(event.side())
                            .isIn(
                                    "player",
                                    "enemy"
                            );

                    assertThat(event.kind())
                            .isIn(
                                    "damage",
                                    "defeat",
                                    "info"
                            );

                    assertThat(event.message())
                            .isNotBlank();
                });
    }

    @Test
    void rejectsDuplicateMonsterIds() {
        BattleRequest.MonsterInput monster =
                monster(
                        FIRST_MONSTER_ID,
                        "Fierce Fox"
                );

        BattleRequest request = request(
                List.of(
                        monster,
                        monster
                )
        );

        assertThatThrownBy(
                () -> battleService.simulate(request)
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                )
                .hasMessageContaining(
                        "duplicate monster IDs"
                );
    }

    @Test
    void changingTeamOrderChangesDeterministicBattleIdentity() {
        BattleResponse first =
                battleService.simulate(
                        request(
                                List.of(
                                        monster(
                                                FIRST_MONSTER_ID,
                                                "Fierce Fox"
                                        ),
                                        monster(
                                                SECOND_MONSTER_ID,
                                                "Stone Bear"
                                        )
                                )
                        )
                );

        BattleResponse reversed =
                battleService.simulate(
                        request(
                                List.of(
                                        monster(
                                                SECOND_MONSTER_ID,
                                                "Stone Bear"
                                        ),
                                        monster(
                                                FIRST_MONSTER_ID,
                                                "Fierce Fox"
                                        )
                                )
                        )
                );

        assertThat(reversed.id())
                .isNotEqualTo(first.id());
    }

    private BattleRequest request(
            List<BattleRequest.MonsterInput> team
    ) {
        return new BattleRequest(
                new BattleRequest.EncounterInput(
                        ENCOUNTER_ID,
                        "Meadow Scouts",
                        false,
                        List.of(
                                new BattleRequest.OpponentInput(
                                        OPPONENT_ID,
                                        "Meadow Pup",
                                        "Fighter",
                                        1
                                )
                        )
                ),
                team
        );
    }

    private BattleRequest.MonsterInput monster(
            UUID id,
            String name
    ) {
        return new BattleRequest.MonsterInput(
                id,
                name,
                "Fighter",
                1,
                new BattleRequest.StatsInput(
                        10,
                        100,
                        60,
                        40
                )
        );
    }
}