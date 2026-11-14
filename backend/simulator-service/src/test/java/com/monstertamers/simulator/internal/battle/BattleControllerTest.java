package com.monstertamers.simulator.internal.battle;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Validator;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.SpringValidatorAdapter;

class BattleControllerTest {

    private static final UUID ENCOUNTER_ID =
            UUID.fromString(
                    "00000000-0000-0000-0000-000000000001"
            );

    private static final UUID MONSTER_ID =
            UUID.fromString(
                    "10000000-0000-0000-0000-000000000001"
            );

    private static final UUID OPPONENT_ID =
            UUID.fromString(
                    "20000000-0000-0000-0000-000000000001"
            );

    private BattleService battleService;
    private ObjectMapper objectMapper;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        battleService = org.mockito.Mockito.mock(
                BattleService.class
        );

        objectMapper = new ObjectMapper();

        jakarta.validation.Validator jakartaValidator =
                jakarta.validation.Validation
                        .buildDefaultValidatorFactory()
                        .getValidator();

        SpringValidatorAdapter validator =
                new SpringValidatorAdapter(
                        jakartaValidator
                );

        mockMvc = MockMvcBuilders
                .standaloneSetup(
                        new BattleController(
                                battleService
                        )
                )
                .setValidator(validator)
                .build();
    }

    @Test
    void simulatesValidInternalBattle()
            throws Exception {
        BattleRequest request = validRequest();

        BattleResponse response =
                new BattleResponse(
                        UUID.fromString(
                                "30000000-0000-0000-0000-000000000001"
                        ),
                        ENCOUNTER_ID,
                        "Meadow Scouts",
                        false,
                        Instant.parse(
                                "2026-01-01T12:00:00Z"
                        ),
                        "victory",
                        3,
                        List.of(
                                new BattleParticipant(
                                        MONSTER_ID,
                                        "Fierce Fox",
                                        "Fighter",
                                        1
                                )
                        ),
                        List.of(
                                new BattleEvent(
                                        "event-1",
                                        1,
                                        "player",
                                        "damage",
                                        "Fierce Fox dealt 10 damage."
                                )
                        )
                );

        when(battleService.simulate(any()))
                .thenReturn(response);

        mockMvc.perform(
                        post("/internal/battles")
                                .contentType(
                                        "application/json"
                                )
                                .content(
                                        objectMapper
                                                .writeValueAsString(
                                                        request
                                                )
                                )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.encounterId")
                                .value(
                                        ENCOUNTER_ID
                                                .toString()
                                )
                )
                .andExpect(
                        jsonPath("$.outcome")
                                .value("victory")
                )
                .andExpect(
                        jsonPath("$.rounds")
                                .value(3)
                )
                .andExpect(
                        jsonPath(
                                "$.participants[0].monsterId"
                        ).value(
                                MONSTER_ID.toString()
                        )
                )
                .andExpect(
                        jsonPath("$.events[0].kind")
                                .value("damage")
                );

        verify(battleService).simulate(any());
    }

    @Test
    void rejectsEmptyTeam()
            throws Exception {
        BattleRequest request =
                new BattleRequest(
                        validEncounter(),
                        List.of()
                );

        mockMvc.perform(
                        post("/internal/battles")
                                .contentType(
                                        "application/json"
                                )
                                .content(
                                        objectMapper
                                                .writeValueAsString(
                                                        request
                                                )
                                )
                )
                .andExpect(
                        status().isBadRequest()
                );

        verify(
                battleService,
                never()
        ).simulate(any());
    }

    @Test
    void rejectsMoreThanThreeMonsters()
            throws Exception {
        BattleRequest.MonsterInput monster =
                validMonster();

        BattleRequest request =
                new BattleRequest(
                        validEncounter(),
                        List.of(
                                monster,
                                monsterWithId(
                                        "10000000-0000-0000-0000-000000000002"
                                ),
                                monsterWithId(
                                        "10000000-0000-0000-0000-000000000003"
                                ),
                                monsterWithId(
                                        "10000000-0000-0000-0000-000000000004"
                                )
                        )
                );

        mockMvc.perform(
                        post("/internal/battles")
                                .contentType(
                                        "application/json"
                                )
                                .content(
                                        objectMapper
                                                .writeValueAsString(
                                                        request
                                                )
                                )
                )
                .andExpect(
                        status().isBadRequest()
                );

        verify(
                battleService,
                never()
        ).simulate(any());
    }

    @Test
    void rejectsMissingEncounter()
            throws Exception {
        String request = """
                {
                  "team": [
                    {
                      "id": "%s",
                      "name": "Fierce Fox",
                      "monsterClass": "Fighter",
                      "level": 1,
                      "stats": {
                        "strength": 10,
                        "hp": 100,
                        "accuracy": 60,
                        "agility": 40
                      }
                    }
                  ]
                }
                """.formatted(MONSTER_ID);

        mockMvc.perform(
                        post("/internal/battles")
                                .contentType(
                                        "application/json"
                                )
                                .content(request)
                )
                .andExpect(
                        status().isBadRequest()
                );

        verify(
                battleService,
                never()
        ).simulate(any());
    }

    @Test
    void rejectsInvalidMonsterStats()
            throws Exception {
        BattleRequest.MonsterInput invalidMonster =
                new BattleRequest.MonsterInput(
                        MONSTER_ID,
                        "Fierce Fox",
                        "Fighter",
                        1,
                        new BattleRequest.StatsInput(
                                0,
                                0,
                                101,
                                0
                        )
                );

        BattleRequest request =
                new BattleRequest(
                        validEncounter(),
                        List.of(invalidMonster)
                );

        mockMvc.perform(
                        post("/internal/battles")
                                .contentType(
                                        "application/json"
                                )
                                .content(
                                        objectMapper
                                                .writeValueAsString(
                                                        request
                                                )
                                )
                )
                .andExpect(
                        status().isBadRequest()
                );

        verify(
                battleService,
                never()
        ).simulate(any());
    }

    private BattleRequest validRequest() {
        return new BattleRequest(
                validEncounter(),
                List.of(validMonster())
        );
    }

    private BattleRequest.EncounterInput
    validEncounter() {
        return new BattleRequest.EncounterInput(
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
        );
    }

    private BattleRequest.MonsterInput
    validMonster() {
        return new BattleRequest.MonsterInput(
                MONSTER_ID,
                "Fierce Fox",
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

    private BattleRequest.MonsterInput
    monsterWithId(
            String id
    ) {
        BattleRequest.MonsterInput monster =
                validMonster();

        return new BattleRequest.MonsterInput(
                UUID.fromString(id),
                monster.name(),
                monster.monsterClass(),
                monster.level(),
                monster.stats()
        );
    }
}