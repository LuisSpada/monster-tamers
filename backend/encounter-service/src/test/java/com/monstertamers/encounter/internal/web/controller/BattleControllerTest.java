package com.monstertamers.encounter.internal.web.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.monstertamers.encounter.internal.application.BattleOrchestrationService;
import com.monstertamers.encounter.internal.domain.BattleOutcome;
import com.monstertamers.encounter.internal.web.dto.request.StartBattleRequest;
import com.monstertamers.encounter.internal.web.dto.response.BattleResultDto;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.core.MethodParameter;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.SpringValidatorAdapter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

class BattleControllerTest {

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

    private static final UUID BATTLE_ID =
            UUID.fromString(
                    "30000000-0000-0000-0000-000000000001"
            );

    private BattleOrchestrationService
            battleOrchestrationService;

    private ObjectMapper objectMapper;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        battleOrchestrationService =
                org.mockito.Mockito.mock(
                        BattleOrchestrationService.class
                );

        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();

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
                                battleOrchestrationService
                        )
                )
                .setValidator(validator)
                .setCustomArgumentResolvers(
                        jwtArgumentResolver()
                )
                .build();
    }

    @Test
    void startsBattleUsingAccountIdFromJwtSubject()
            throws Exception {
        List<UUID> monsterIds = List.of(
                SECOND_MONSTER_ID,
                FIRST_MONSTER_ID
        );

        StartBattleRequest expectedRequest =
                new StartBattleRequest(monsterIds);

        BattleResultDto result =
                successfulResult(monsterIds);

        when(
                battleOrchestrationService
                        .startBattle(
                                eq(ACCOUNT_ID),
                                eq(ENCOUNTER_ID),
                                any(StartBattleRequest.class)
                        )
        ).thenReturn(result);

        mockMvc.perform(
                        post(
                                "/api/encounters/{encounterId}/battles",
                                ENCOUNTER_ID
                        )
                                .contentType(
                                        "application/json"
                                )
                                .content(
                                        objectMapper
                                                .writeValueAsString(
                                                        expectedRequest
                                                )
                                )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.id")
                                .value(
                                        BATTLE_ID.toString()
                                )
                )
                .andExpect(
                        jsonPath("$.encounterId")
                                .value(
                                        ENCOUNTER_ID.toString()
                                )
                )
                .andExpect(
                        jsonPath("$.encounterName")
                                .value("Meadow Scouts")
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
                                SECOND_MONSTER_ID.toString()
                        )
                )
                .andExpect(
                        jsonPath(
                                "$.participants[1].monsterId"
                        ).value(
                                FIRST_MONSTER_ID.toString()
                        )
                )
                .andExpect(
                        jsonPath("$.events[0].kind")
                                .value("damage")
                );

        ArgumentCaptor<StartBattleRequest> requestCaptor =
                ArgumentCaptor.forClass(
                        StartBattleRequest.class
                );

        verify(battleOrchestrationService)
                .startBattle(
                        eq(ACCOUNT_ID),
                        eq(ENCOUNTER_ID),
                        requestCaptor.capture()
                );

        assertThat(
                requestCaptor
                        .getValue()
                        .monsterIds()
        ).containsExactly(
                SECOND_MONSTER_ID,
                FIRST_MONSTER_ID
        );
    }

    @Test
    void rejectsEmptyTeamBeforeCallingOrchestration()
            throws Exception {
        mockMvc.perform(
                        post(
                                "/api/encounters/{encounterId}/battles",
                                ENCOUNTER_ID
                        )
                                .contentType(
                                        "application/json"
                                )
                                .content(
                                        """
                                        {
                                          "monsterIds": []
                                        }
                                        """
                                )
                )
                .andExpect(status().isBadRequest());

        verifyOrchestrationWasNotCalled();
    }

    @Test
    void rejectsMoreThanThreeMonstersBeforeCallingOrchestration()
            throws Exception {
        mockMvc.perform(
                        post(
                                "/api/encounters/{encounterId}/battles",
                                ENCOUNTER_ID
                        )
                                .contentType(
                                        "application/json"
                                )
                                .content(
                                        """
                                        {
                                          "monsterIds": [
                                            "10000000-0000-0000-0000-000000000001",
                                            "10000000-0000-0000-0000-000000000002",
                                            "10000000-0000-0000-0000-000000000003",
                                            "10000000-0000-0000-0000-000000000004"
                                          ]
                                        }
                                        """
                                )
                )
                .andExpect(status().isBadRequest());

        verifyOrchestrationWasNotCalled();
    }

    @Test
    void rejectsMissingMonsterIdsBeforeCallingOrchestration()
            throws Exception {
        mockMvc.perform(
                        post(
                                "/api/encounters/{encounterId}/battles",
                                ENCOUNTER_ID
                        )
                                .contentType(
                                        "application/json"
                                )
                                .content("{}")
                )
                .andExpect(status().isBadRequest());

        verifyOrchestrationWasNotCalled();
    }

    @Test
    void rejectsNullMonsterIdBeforeCallingOrchestration()
            throws Exception {
        mockMvc.perform(
                        post(
                                "/api/encounters/{encounterId}/battles",
                                ENCOUNTER_ID
                        )
                                .contentType(
                                        "application/json"
                                )
                                .content(
                                        """
                                        {
                                          "monsterIds": [
                                            null
                                          ]
                                        }
                                        """
                                )
                )
                .andExpect(status().isBadRequest());

        verifyOrchestrationWasNotCalled();
    }

    @Test
    void rejectsMalformedEncounterIdBeforeCallingOrchestration()
            throws Exception {
        mockMvc.perform(
                        post(
                                "/api/encounters/not-a-uuid/battles"
                        )
                                .contentType(
                                        "application/json"
                                )
                                .content(
                                        """
                                        {
                                          "monsterIds": [
                                            "44ed4dd1-6abd-4991-97cc-86bf95429a10"
                                          ]
                                        }
                                        """
                                )
                )
                .andExpect(status().isBadRequest());

        verifyOrchestrationWasNotCalled();
    }

    private void verifyOrchestrationWasNotCalled() {
        verify(
                battleOrchestrationService,
                never()
        ).startBattle(
                any(),
                any(),
                any()
        );
    }

    private HandlerMethodArgumentResolver
    jwtArgumentResolver() {
        Jwt jwt = Jwt.withTokenValue(
                        "battle-controller-test-token"
                )
                .header("alg", "none")
                .subject(ACCOUNT_ID.toString())
                .build();

        return new HandlerMethodArgumentResolver() {

            @Override
            public boolean supportsParameter(
                    MethodParameter parameter
            ) {
                return parameter.hasParameterAnnotation(
                        AuthenticationPrincipal.class
                ) && Jwt.class.isAssignableFrom(
                        parameter.getParameterType()
                );
            }

            @Override
            public Object resolveArgument(
                    MethodParameter parameter,
                    ModelAndViewContainer container,
                    NativeWebRequest request,
                    WebDataBinderFactory binderFactory
            ) {
                return jwt;
            }
        };
    }

    private BattleResultDto successfulResult(
            List<UUID> participantIds
    ) {
        List<BattleResultDto.ParticipantDto>
                participants =
                participantIds
                        .stream()
                        .map(monsterId ->
                                new BattleResultDto.ParticipantDto(
                                        monsterId,
                                        monsterId.equals(
                                                SECOND_MONSTER_ID
                                        )
                                                ? "Stone Bear"
                                                : "Fierce Fox",
                                        monsterId.equals(
                                                SECOND_MONSTER_ID
                                        )
                                                ? "Guardian"
                                                : "Fighter",
                                        monsterId.equals(
                                                SECOND_MONSTER_ID
                                        )
                                                ? 2
                                                : 1
                                )
                        )
                        .toList();

        return new BattleResultDto(
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
                        new BattleResultDto.EventDto(
                                "event-1",
                                1,
                                "player",
                                "damage",
                                "Stone Bear dealt 10 damage."
                        ),
                        new BattleResultDto.EventDto(
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
