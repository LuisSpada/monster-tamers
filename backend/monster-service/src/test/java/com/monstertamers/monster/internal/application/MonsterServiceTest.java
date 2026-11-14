package com.monstertamers.monster.internal.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.monstertamers.monster.internal.domain.Monster;
import com.monstertamers.monster.internal.domain.MonsterClass;
import com.monstertamers.monster.internal.persistence.MonsterRepository;
import com.monstertamers.monster.internal.web.dto.response.MonsterDto;
import com.monstertamers.monster.internal.web.mapper.MonsterMapper;
import com.monstertamers.web.error.ApiException;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

@ExtendWith(MockitoExtension.class)
class MonsterServiceTest {

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

    private static final UUID UNKNOWN_MONSTER_ID =
            UUID.fromString(
                    "99999999-9999-9999-9999-999999999999"
            );

    @Mock
    private MonsterRepository monsterRepository;

    @Mock
    private MonsterMapper monsterMapper;

    @Mock
    private MonsterGenerator monsterGenerator;

    @InjectMocks
    private MonsterService monsterService;

    @Captor
    private ArgumentCaptor<List<Monster>> savedCaptor;

    @Test
    @DisplayName(
            "initCollection skips an account that already has monsters"
    )
    void initCollectionExistingMonstersSkipped() {
        when(
                monsterRepository
                        .existsByAccountId(ACCOUNT_ID)
        ).thenReturn(true);

        monsterService.initCollection(ACCOUNT_ID);

        verify(
                monsterRepository,
                never()
        ).saveAll(any());
    }

    @Test
    @DisplayName(
            "initCollection gives an empty collection three account-owned monsters"
    )
    void initCollectionEmptyCollectionSucceeds() {
        when(
                monsterRepository
                        .existsByAccountId(ACCOUNT_ID)
        ).thenReturn(false);

        when(monsterGenerator.generate())
                .thenAnswer(invocation ->
                        new Monster()
                );

        monsterService.initCollection(ACCOUNT_ID);

        verify(monsterRepository)
                .saveAll(savedCaptor.capture());

        assertThat(savedCaptor.getValue())
                .hasSize(3)
                .allSatisfy(monster ->
                        assertThat(
                                monster.getAccountId()
                        ).isEqualTo(ACCOUNT_ID)
                );
    }

    @Test
    void battleLookupPreservesRequestedOrder() {
        Monster first = monster(
                FIRST_MONSTER_ID,
                "Fierce Fox"
        );

        Monster second = monster(
                SECOND_MONSTER_ID,
                "Stone Bear"
        );

        MonsterDto firstDto = dto(
                FIRST_MONSTER_ID,
                "Fierce Fox"
        );

        MonsterDto secondDto = dto(
                SECOND_MONSTER_ID,
                "Stone Bear"
        );

        when(
                monsterRepository
                        .findAllByIdInAndAccountId(
                                List.of(
                                        SECOND_MONSTER_ID,
                                        FIRST_MONSTER_ID
                                ),
                                ACCOUNT_ID
                        )
        ).thenReturn(
                List.of(first, second)
        );

        when(monsterMapper.toDto(first))
                .thenReturn(firstDto);

        when(monsterMapper.toDto(second))
                .thenReturn(secondDto);

        List<MonsterDto> result =
                monsterService.findOwnedForBattle(
                        ACCOUNT_ID,
                        List.of(
                                SECOND_MONSTER_ID,
                                FIRST_MONSTER_ID
                        )
                );

        assertThat(result)
                .containsExactly(
                        secondDto,
                        firstDto
                );

        verify(monsterRepository)
                .findAllByIdInAndAccountId(
                        List.of(
                                SECOND_MONSTER_ID,
                                FIRST_MONSTER_ID
                        ),
                        ACCOUNT_ID
                );
    }

    @Test
    void battleLookupRejectsUnownedMonster() {
        Monster first = monster(
                FIRST_MONSTER_ID,
                "Fierce Fox"
        );

        when(
                monsterRepository
                        .findAllByIdInAndAccountId(
                                List.of(
                                        FIRST_MONSTER_ID,
                                        UNKNOWN_MONSTER_ID
                                ),
                                ACCOUNT_ID
                        )
        ).thenReturn(List.of(first));

        assertThatThrownBy(() ->
                monsterService.findOwnedForBattle(
                        ACCOUNT_ID,
                        List.of(
                                FIRST_MONSTER_ID,
                                UNKNOWN_MONSTER_ID
                        )
                )
        )
                .isInstanceOf(ApiException.class)
                .satisfies(exception ->
                        assertThat(
                                ((ApiException) exception)
                                        .getStatusCode()
                        ).isEqualTo(
                                HttpStatus
                                        .UNPROCESSABLE_CONTENT
                        )
                );

        verifyNoInteractions(monsterMapper);
    }

    @Test
    void battleLookupRejectsDuplicateIds() {
        assertThatThrownBy(() ->
                monsterService.findOwnedForBattle(
                        ACCOUNT_ID,
                        List.of(
                                FIRST_MONSTER_ID,
                                FIRST_MONSTER_ID
                        )
                )
        )
                .isInstanceOf(ApiException.class)
                .satisfies(exception ->
                        assertThat(
                                ((ApiException) exception)
                                        .getStatusCode()
                        ).isEqualTo(
                                HttpStatus
                                        .UNPROCESSABLE_CONTENT
                        )
                );

        verifyNoInteractions(
                monsterRepository,
                monsterMapper
        );
    }

    @Test
    void battleLookupRejectsEmptyTeam() {
        assertThatThrownBy(() ->
                monsterService.findOwnedForBattle(
                        ACCOUNT_ID,
                        List.of()
                )
        )
                .isInstanceOf(ApiException.class);

        verifyNoInteractions(
                monsterRepository,
                monsterMapper
        );
    }

    @Test
    void battleLookupRejectsMoreThanThreeIds() {
        List<UUID> ids = List.of(
                FIRST_MONSTER_ID,
                SECOND_MONSTER_ID,
                UUID.fromString(
                        "10000000-0000-0000-0000-000000000003"
                ),
                UUID.fromString(
                        "10000000-0000-0000-0000-000000000004"
                )
        );

        assertThatThrownBy(() ->
                monsterService.findOwnedForBattle(
                        ACCOUNT_ID,
                        ids
                )
        )
                .isInstanceOf(ApiException.class);

        verifyNoInteractions(
                monsterRepository,
                monsterMapper
        );
    }

    private Monster monster(
            UUID id,
            String name
    ) {
        Monster monster = new Monster();

        monster.setId(id);
        monster.setAccountId(ACCOUNT_ID);
        monster.setName(name);
        monster.setMonsterClass(
                MonsterClass.FIGHTER
        );

        return monster;
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