package com.monstertamers.encounter.internal.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.monstertamers.encounter.internal.domain.BattleOutcome;
import com.monstertamers.encounter.internal.domain.Encounter;
import com.monstertamers.encounter.internal.domain.EncounterOpponent;
import com.monstertamers.encounter.internal.integration.monster.BattleMonsterDto;
import com.monstertamers.encounter.internal.integration.monster.BattleMonsterRequest;
import com.monstertamers.encounter.internal.integration.monster.BattleMonsterStatsDto;
import com.monstertamers.encounter.internal.integration.monster.MonsterClient;
import com.monstertamers.encounter.internal.integration.simulator.SimulatorBattleRequest;
import com.monstertamers.encounter.internal.integration.simulator.SimulatorBattleResponse;
import com.monstertamers.encounter.internal.integration.simulator.SimulatorClient;
import com.monstertamers.encounter.internal.persistence.AccountEncounterProgressRepository;
import com.monstertamers.encounter.internal.persistence.EncounterOpponentRepository;
import com.monstertamers.encounter.internal.persistence.EncounterRepository;
import com.monstertamers.encounter.internal.web.dto.request.StartBattleRequest;
import com.monstertamers.encounter.internal.web.dto.response.BattleResultDto;
import com.monstertamers.encounter.internal.web.dto.response.EncounterShortDto;
import com.monstertamers.web.error.ApiException;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.RestClientException;

@ExtendWith(MockitoExtension.class)
class BattleOrchestrationServiceTest {

    private static final UUID ACCOUNT_ID =
            UUID.fromString(
                    "32923c1a-e469-4014-b957-3e56f88a9a10"
            );

    private static final UUID ENCOUNTER_ID =
            UUID.fromString(
                    "00000000-0000-0000-0000-000000000001"
            );

    private static final UUID FIRST_MONSTER_ID =
            UUID.fromString(
                    "44ed4dd1-6abd-4991-97cc-86bf95429a10"
            );

    private static final UUID SECOND_MONSTER_ID =
            UUID.fromString(
                    "729827c5-0848-4898-bd4a-959a6d49fb92"
            );

    private static final UUID OPPONENT_ID =
            UUID.fromString(
                    "10000000-0000-0000-0000-000000000001"
            );

    private static final UUID BATTLE_ID =
            UUID.fromString(
                    "30000000-0000-0000-0000-000000000001"
            );

    @Mock
    private EncounterRepository encounterRepository;

    @Mock
    private EncounterOpponentRepository opponentRepository;

    @Mock
    private AccountEncounterProgressRepository progressRepository;

    @Mock
    private MonsterClient monsterClient;

    @Mock
    private SimulatorClient simulatorClient;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private BattleHistoryService battleHistoryService;

    @InjectMocks
    private BattleOrchestrationService service;

    @Captor
    private ArgumentCaptor<BattleMonsterRequest>
            monsterRequestCaptor;

    @Captor
    private ArgumentCaptor<SimulatorBattleRequest>
            simulatorRequestCaptor;

    @Test
    void startsBattleWithBackendControlledDataAndPreservesOrder() {
        Encounter encounter = availableEncounter();
        EncounterOpponent opponent = opponent();

        List<UUID> requestedIds = List.of(
                SECOND_MONSTER_ID,
                FIRST_MONSTER_ID
        );

        List<BattleMonsterDto> monsters = List.of(
                monster(
                        SECOND_MONSTER_ID,
                        "Stone Bear",
                        "Guardian",
                        2,
                        new BattleMonsterStatsDto(
                                8,
                                140,
                                55,
                                25
                        )
                ),
                monster(
                        FIRST_MONSTER_ID,
                        "Fierce Fox",
                        "Fighter",
                        1,
                        new BattleMonsterStatsDto(
                                12,
                                95,
                                70,
                                50
                        )
                )
        );

        SimulatorBattleResponse simulatorResponse =
                successfulResponse(requestedIds);

        when(
                encounterRepository.findById(
                        ENCOUNTER_ID
                )
        ).thenReturn(Optional.of(encounter));

        when(
                progressRepository
                        .findByAccountIdAndEncounterId(
                                ACCOUNT_ID,
                                ENCOUNTER_ID
                        )
        ).thenReturn(Optional.empty());

        when(
                opponentRepository
                        .findAllByEncounterIdOrderByIdAsc(
                                ENCOUNTER_ID
                        )
        ).thenReturn(List.of(opponent));

        when(
                monsterClient.loadBattleMonsters(
                        any(),
                        any()
                )
        ).thenReturn(monsters);

        when(
                simulatorClient.simulate(any())
        ).thenReturn(simulatorResponse);

        BattleResultDto savedResult = new BattleResultDto(
                BATTLE_ID,
                new EncounterShortDto(ENCOUNTER_ID, "Meadow Scouts", false),
                Instant.parse("2026-11-14T12:00:00Z"),
                BattleOutcome.victory,
                3,
                List.of(),
                List.of()
        );

        when(
                battleHistoryService.save(
                        ACCOUNT_ID,
                        encounter,
                        simulatorResponse
                )
        ).thenReturn(savedResult);

        BattleResultDto result = service.startBattle(
                ACCOUNT_ID,
                ENCOUNTER_ID,
                new StartBattleRequest(requestedIds)
        );

        assertThat(result)
                .isSameAs(savedResult);

        verify(monsterClient)
                .loadBattleMonsters(
                        eq(ACCOUNT_ID),
                        monsterRequestCaptor.capture()
                );

        assertThat(
                monsterRequestCaptor
                        .getValue()
                        .monsterIds()
        ).containsExactly(
                SECOND_MONSTER_ID,
                FIRST_MONSTER_ID
        );

        verify(simulatorClient)
                .simulate(
                        simulatorRequestCaptor.capture()
                );

        SimulatorBattleRequest simulatorRequest =
                simulatorRequestCaptor.getValue();

        assertThat(
                simulatorRequest.encounter().id()
        ).isEqualTo(ENCOUNTER_ID);

        assertThat(
                simulatorRequest.encounter().name()
        ).isEqualTo("Meadow Scouts");

        assertThat(
                simulatorRequest.encounter().finalBoss()
        ).isFalse();

        assertThat(
                simulatorRequest.encounter().opponents()
        ).singleElement().satisfies(input -> {
            assertThat(input.id())
                    .isEqualTo(OPPONENT_ID);

            assertThat(input.name())
                    .isEqualTo("Meadow Pup");

            assertThat(input.monsterClass())
                    .isEqualTo("Fighter");

            assertThat(input.level())
                    .isEqualTo(1);
        });

        assertThat(simulatorRequest.team())
                .extracting(
                        SimulatorBattleRequest.MonsterInput::id
                )
                .containsExactly(
                        SECOND_MONSTER_ID,
                        FIRST_MONSTER_ID
                );

        SimulatorBattleRequest.MonsterInput firstInput =
                simulatorRequest.team().getFirst();

        assertThat(firstInput.name())
                .isEqualTo("Stone Bear");

        assertThat(firstInput.monsterClass())
                .isEqualTo("Guardian");

        assertThat(firstInput.level())
                .isEqualTo(2);

        assertThat(firstInput.stats().strength())
                .isEqualTo(8);

        assertThat(firstInput.stats().hp())
                .isEqualTo(140);

        assertThat(firstInput.stats().accuracy())
                .isEqualTo(55);

        assertThat(firstInput.stats().agility())
                .isEqualTo(25);
    }

    @Test
    void rejectsUnknownEncounterBeforeCallingServices() {
        when(
                encounterRepository.findById(
                        ENCOUNTER_ID
                )
        ).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                service.startBattle(
                        ACCOUNT_ID,
                        ENCOUNTER_ID,
                        request(FIRST_MONSTER_ID)
                )
        )
                .isInstanceOf(ApiException.class)
                .satisfies(exception ->
                        assertThat(
                                ((ApiException) exception)
                                        .getStatusCode()
                        ).isEqualTo(HttpStatus.NOT_FOUND)
                );

        verifyNoInteractions(
                opponentRepository,
                progressRepository,
                monsterClient,
                simulatorClient
        );
    }

    @Test
    void rejectsLockedEncounterBeforeLoadingMonsters() {
        Encounter encounter = availableEncounter();
        encounter.setOrder(2);

        when(
                encounterRepository.findById(
                        ENCOUNTER_ID
                )
        ).thenReturn(Optional.of(encounter));

        when(
                progressRepository
                        .findByAccountIdAndEncounterId(
                                ACCOUNT_ID,
                                ENCOUNTER_ID
                        )
        ).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                service.startBattle(
                        ACCOUNT_ID,
                        ENCOUNTER_ID,
                        request(FIRST_MONSTER_ID)
                )
        )
                .isInstanceOf(ApiException.class)
                .satisfies(exception ->
                        assertThat(
                                ((ApiException) exception)
                                        .getStatusCode()
                        ).isEqualTo(HttpStatus.CONFLICT)
                );

        verifyNoInteractions(
                opponentRepository,
                monsterClient,
                simulatorClient
        );
    }

    @Test
    void rejectsDuplicateTeamBeforeRepositoryLookup() {
        StartBattleRequest request =
                new StartBattleRequest(
                        List.of(
                                FIRST_MONSTER_ID,
                                FIRST_MONSTER_ID
                        )
                );

        assertThatThrownBy(() ->
                service.startBattle(
                        ACCOUNT_ID,
                        ENCOUNTER_ID,
                        request
                )
        )
                .isInstanceOf(ApiException.class)
                .satisfies(exception ->
                        assertThat(
                                ((ApiException) exception)
                                        .getStatusCode()
                        ).isEqualTo(
                                HttpStatus.UNPROCESSABLE_CONTENT
                        )
                );

        verifyNoInteractions(
                encounterRepository,
                opponentRepository,
                progressRepository,
                monsterClient,
                simulatorClient
        );
    }

    @Test
    void rejectsEmptyTeamBeforeRepositoryLookup() {
        assertThatThrownBy(() ->
                service.startBattle(
                        ACCOUNT_ID,
                        ENCOUNTER_ID,
                        new StartBattleRequest(List.of())
                )
        )
                .isInstanceOf(ApiException.class)
                .satisfies(exception ->
                        assertThat(
                                ((ApiException) exception)
                                        .getStatusCode()
                        ).isEqualTo(
                                HttpStatus.UNPROCESSABLE_CONTENT
                        )
                );

        verifyNoInteractions(
                encounterRepository,
                opponentRepository,
                progressRepository,
                monsterClient,
                simulatorClient
        );
    }

    @Test
    void rejectsMoreThanThreeMonstersBeforeRepositoryLookup() {
        StartBattleRequest request =
                new StartBattleRequest(
                        List.of(
                                FIRST_MONSTER_ID,
                                SECOND_MONSTER_ID,
                                UUID.fromString(
                                        "10000000-0000-0000-0000-000000000003"
                                ),
                                UUID.fromString(
                                        "10000000-0000-0000-0000-000000000004"
                                )
                        )
                );

        assertThatThrownBy(() ->
                service.startBattle(
                        ACCOUNT_ID,
                        ENCOUNTER_ID,
                        request
                )
        )
                .isInstanceOf(ApiException.class)
                .satisfies(exception ->
                        assertThat(
                                ((ApiException) exception)
                                        .getStatusCode()
                        ).isEqualTo(
                                HttpStatus.UNPROCESSABLE_CONTENT
                        )
                );

        verifyNoInteractions(
                encounterRepository,
                opponentRepository,
                progressRepository,
                monsterClient,
                simulatorClient
        );
    }

    @Test
    void rejectsMonsterResponseWithChangedOrder() {
        prepareAvailableEncounter();

        when(
                monsterClient.loadBattleMonsters(
                        any(),
                        any()
                )
        ).thenReturn(
                List.of(
                        monster(
                                FIRST_MONSTER_ID,
                                "Fierce Fox",
                                "Fighter",
                                1,
                                defaultStats()
                        ),
                        monster(
                                SECOND_MONSTER_ID,
                                "Stone Bear",
                                "Guardian",
                                2,
                                defaultStats()
                        )
                )
        );

        StartBattleRequest request =
                new StartBattleRequest(
                        List.of(
                                SECOND_MONSTER_ID,
                                FIRST_MONSTER_ID
                        )
                );

        assertThatThrownBy(() ->
                service.startBattle(
                        ACCOUNT_ID,
                        ENCOUNTER_ID,
                        request
                )
        )
                .isInstanceOf(ApiException.class)
                .satisfies(exception ->
                        assertThat(
                                ((ApiException) exception)
                                        .getStatusCode()
                        ).isEqualTo(HttpStatus.BAD_GATEWAY)
                );

        verify(
                simulatorClient,
                never()
        ).simulate(any());
    }

    @Test
    void mapsMonsterTransportFailureToBadGateway() {
        prepareAvailableEncounter();

        when(
                monsterClient.loadBattleMonsters(
                        any(),
                        any()
                )
        ).thenThrow(
                new RestClientException(
                        "Monster Service unavailable"
                )
        );

        assertThatThrownBy(() ->
                service.startBattle(
                        ACCOUNT_ID,
                        ENCOUNTER_ID,
                        request(FIRST_MONSTER_ID)
                )
        )
                .isInstanceOf(ApiException.class)
                .satisfies(exception ->
                        assertThat(
                                ((ApiException) exception)
                                        .getStatusCode()
                        ).isEqualTo(HttpStatus.BAD_GATEWAY)
                );

        verifyNoInteractions(simulatorClient);
    }

    @Test
    void mapsSimulatorTransportFailureToServiceUnavailable() {
        prepareAvailableEncounter();

        when(
                monsterClient.loadBattleMonsters(
                        any(),
                        any()
                )
        ).thenReturn(
                List.of(
                        monster(
                                FIRST_MONSTER_ID,
                                "Fierce Fox",
                                "Fighter",
                                1,
                                defaultStats()
                        )
                )
        );

        when(
                simulatorClient.simulate(any())
        ).thenThrow(
                new RestClientException(
                        "Simulator unavailable"
                )
        );

        assertThatThrownBy(() ->
                service.startBattle(
                        ACCOUNT_ID,
                        ENCOUNTER_ID,
                        request(FIRST_MONSTER_ID)
                )
        )
                .isInstanceOf(ApiException.class)
                .satisfies(exception ->
                        assertThat(
                                ((ApiException) exception)
                                        .getStatusCode()
                        ).isEqualTo(
                                HttpStatus.SERVICE_UNAVAILABLE
                        )
                );
    }

    @Test
    void rejectsSimulatorResponseForDifferentEncounter() {
        prepareAvailableEncounter();

        when(
                monsterClient.loadBattleMonsters(
                        any(),
                        any()
                )
        ).thenReturn(
                List.of(
                        monster(
                                FIRST_MONSTER_ID,
                                "Fierce Fox",
                                "Fighter",
                                1,
                                defaultStats()
                        )
                )
        );

        SimulatorBattleResponse invalidResponse =
                new SimulatorBattleResponse(
                        BATTLE_ID,
                        UUID.fromString(
                                "99999999-9999-9999-9999-999999999999"
                        ),
                        "Wrong Encounter",
                        false,
                        Instant.parse(
                                "2026-11-14T12:00:00Z"
                        ),
                        BattleOutcome.victory,
                        2,
                        List.of(),
                        List.of()
                );

        when(
                simulatorClient.simulate(any())
        ).thenReturn(invalidResponse);

        assertThatThrownBy(() ->
                service.startBattle(
                        ACCOUNT_ID,
                        ENCOUNTER_ID,
                        request(FIRST_MONSTER_ID)
                )
        )
                .isInstanceOf(ApiException.class)
                .satisfies(exception ->
                        assertThat(
                                ((ApiException) exception)
                                        .getStatusCode()
                        ).isEqualTo(HttpStatus.BAD_GATEWAY)
                );
    }

    @Test
    void rejectsSimulatorResponseWithInvalidOutcome() {
        prepareAvailableEncounter();

        when(
                monsterClient.loadBattleMonsters(
                        any(),
                        any()
                )
        ).thenReturn(
                List.of(
                        monster(
                                FIRST_MONSTER_ID,
                                "Fierce Fox",
                                "Fighter",
                                1,
                                defaultStats()
                        )
                )
        );

        SimulatorBattleResponse invalidResponse =
                new SimulatorBattleResponse(
                        BATTLE_ID,
                        ENCOUNTER_ID,
                        "Meadow Scouts",
                        false,
                        Instant.parse(
                                "2026-11-14T12:00:00Z"
                        ),
                        null,
                        2,
                        List.of(),
                        List.of()
                );

        when(
                simulatorClient.simulate(any())
        ).thenReturn(invalidResponse);

        assertThatThrownBy(() ->
                service.startBattle(
                        ACCOUNT_ID,
                        ENCOUNTER_ID,
                        request(FIRST_MONSTER_ID)
                )
        )
                .isInstanceOf(ApiException.class)
                .satisfies(exception ->
                        assertThat(
                                ((ApiException) exception)
                                        .getStatusCode()
                        ).isEqualTo(HttpStatus.BAD_GATEWAY)
                );
    }

    private void prepareAvailableEncounter() {
        when(
                encounterRepository.findById(
                        ENCOUNTER_ID
                )
        ).thenReturn(
                Optional.of(availableEncounter())
        );

        when(
                progressRepository
                        .findByAccountIdAndEncounterId(
                                ACCOUNT_ID,
                                ENCOUNTER_ID
                        )
        ).thenReturn(Optional.empty());

        when(
                opponentRepository
                        .findAllByEncounterIdOrderByIdAsc(
                                ENCOUNTER_ID
                        )
        ).thenReturn(List.of(opponent()));
    }

    private StartBattleRequest request(
            UUID... monsterIds
    ) {
        return new StartBattleRequest(
                List.of(monsterIds)
        );
    }

    private Encounter availableEncounter() {
        Encounter encounter = new Encounter();

        encounter.setId(ENCOUNTER_ID);
        encounter.setOrder(1);
        encounter.setName("Meadow Scouts");
        encounter.setDescription(
                "A gentle first encounter."
        );
        encounter.setEggChance(20);
        encounter.setFinalBoss(false);
        encounter.setRewardClasses(
                List.of(
                        "Fighter",
                        "Trickster"
                )
        );

        return encounter;
    }

    private EncounterOpponent opponent() {
        EncounterOpponent opponent =
                new EncounterOpponent();

        opponent.setId(OPPONENT_ID);
        opponent.setEncounterId(ENCOUNTER_ID);
        opponent.setName("Meadow Pup");
        opponent.setMonsterClass("Fighter");
        opponent.setLevel(1);
        opponent.setAvatarVariant(0);

        return opponent;
    }

    private BattleMonsterDto monster(
            UUID id,
            String name,
            String monsterClass,
            int level,
            BattleMonsterStatsDto stats
    ) {
        return new BattleMonsterDto(
                id,
                name,
                monsterClass,
                level,
                stats
        );
    }

    private BattleMonsterStatsDto defaultStats() {
        return new BattleMonsterStatsDto(
                10,
                100,
                60,
                40
        );
    }

    private SimulatorBattleResponse successfulResponse(
            List<UUID> participantIds
    ) {
        List<SimulatorBattleResponse.Participant>
                participants =
                participantIds
                        .stream()
                        .map(id ->
                                new SimulatorBattleResponse.Participant(
                                        id,
                                        id.equals(SECOND_MONSTER_ID)
                                                ? "Stone Bear"
                                                : "Fierce Fox",
                                        id.equals(SECOND_MONSTER_ID)
                                                ? "Guardian"
                                                : "Fighter",
                                        id.equals(SECOND_MONSTER_ID)
                                                ? 2
                                                : 1
                                )
                        )
                        .toList();

        return new SimulatorBattleResponse(
                BATTLE_ID,
                ENCOUNTER_ID,
                "Meadow Scouts",
                false,
                Instant.parse(
                        "2026-11-14T12:00:00Z"
                ),
                BattleOutcome.victory,
                3,
                participants,
                List.of(
                        new SimulatorBattleResponse.Event(
                                "event-1",
                                1,
                                "player",
                                "damage",
                                "Stone Bear dealt 10 damage."
                        ),
                        new SimulatorBattleResponse.Event(
                                "event-2",
                                3,
                                "player",
                                "info",
                                "Your team won the battle."
                        )
                )
        );
    }
}
