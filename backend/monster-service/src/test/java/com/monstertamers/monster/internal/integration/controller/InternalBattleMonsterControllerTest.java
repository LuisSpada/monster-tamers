package com.monstertamers.monster.internal.integration.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.monstertamers.monster.internal.application.MonsterService;
import com.monstertamers.monster.internal.domain.MonsterClass;
import com.monstertamers.monster.internal.integration.dto.BattleMonsterRequest;
import com.monstertamers.monster.internal.web.dto.response.MonsterDto;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.validation.beanvalidation.SpringValidatorAdapter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class InternalBattleMonsterControllerTest {

    private static final UUID ACCOUNT_ID =
            UUID.fromString(
                    "32923c1a-e469-4014-b957-3e56f88a9a10"
            );

    private static final UUID FIRST_MONSTER_ID =
            UUID.fromString(
                    "44ed4dd1-6abd-4991-97cc-86bf95429a10"
            );

    private static final UUID SECOND_MONSTER_ID =
            UUID.fromString(
                    "729827c5-0848-4898-bd4a-959a6d49fb92"
            );

    private MonsterService monsterService;
    private ObjectMapper objectMapper;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        monsterService =
                mock(MonsterService.class);

        objectMapper = new ObjectMapper();

        jakarta.validation.Validator
                jakartaValidator =
                jakarta.validation.Validation
                        .buildDefaultValidatorFactory()
                        .getValidator();

        SpringValidatorAdapter validator =
                new SpringValidatorAdapter(
                        jakartaValidator
                );

        mockMvc = MockMvcBuilders
                .standaloneSetup(
                        new InternalBattleMonsterController(
                                monsterService
                        )
                )
                .setValidator(validator)
                .build();
    }

    @Test
    void returnsOrderedAccountOwnedMonsters()
            throws Exception {
        List<UUID> requestedIds = List.of(
                SECOND_MONSTER_ID,
                FIRST_MONSTER_ID
        );

        List<MonsterDto> response = List.of(
                dto(
                        SECOND_MONSTER_ID,
                        "Stone Bear"
                ),
                dto(
                        FIRST_MONSTER_ID,
                        "Fierce Fox"
                )
        );

        when(
                monsterService.findOwnedForBattle(
                        ACCOUNT_ID,
                        requestedIds
                )
        ).thenReturn(response);

        mockMvc.perform(
                        post(
                                "/internal/accounts/{accountId}/battle-monsters",
                                ACCOUNT_ID
                        )
                                .contentType(
                                        "application/json"
                                )
                                .content(
                                        objectMapper
                                                .writeValueAsString(
                                                        new BattleMonsterRequest(
                                                                requestedIds
                                                        )
                                                )
                                )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$[0].id")
                                .value(
                                        SECOND_MONSTER_ID
                                                .toString()
                                )
                )
                .andExpect(
                        jsonPath("$[1].id")
                                .value(
                                        FIRST_MONSTER_ID
                                                .toString()
                                )
                );

        verify(monsterService)
                .findOwnedForBattle(
                        ACCOUNT_ID,
                        requestedIds
                );
    }

    @Test
    void rejectsEmptyTeamBeforeCallingService()
            throws Exception {
        mockMvc.perform(
                        post(
                                "/internal/accounts/{accountId}/battle-monsters",
                                ACCOUNT_ID
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

        verify(
                monsterService,
                never()
        ).findOwnedForBattle(
                eq(ACCOUNT_ID),
                any()
        );
    }

    @Test
    void rejectsMoreThanThreeMonsters()
            throws Exception {
        mockMvc.perform(
                        post(
                                "/internal/accounts/{accountId}/battle-monsters",
                                ACCOUNT_ID
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

        verify(
                monsterService,
                never()
        ).findOwnedForBattle(
                eq(ACCOUNT_ID),
                any()
        );
    }

    private MonsterDto dto(
            UUID id,
            String name
    ) {
        return MonsterDto.builder()
                .id(id)
                .name(name)
                .monsterClass(
                        MonsterClass.FIGHTER
                )
                .avatarVariant(0)
                .talent(100)
                .upgrades(List.of())
                .build();
    }
}