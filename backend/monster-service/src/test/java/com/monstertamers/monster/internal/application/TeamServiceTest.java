package com.monstertamers.monster.internal.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.monstertamers.monster.internal.domain.Team;
import com.monstertamers.monster.internal.persistence.MonsterRepository;
import com.monstertamers.monster.internal.persistence.TeamRepository;
import com.monstertamers.monster.internal.web.dto.request.SaveTeamRequest;
import com.monstertamers.monster.internal.web.dto.response.TeamDto;
import com.monstertamers.web.error.ApiException;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

@ExtendWith(MockitoExtension.class)
class TeamServiceTest {

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

    @Mock
    private TeamRepository teamRepository;

    @Mock
    private MonsterRepository monsterRepository;

    @InjectMocks
    private TeamService teamService;

    @Test
    void findWithoutSavedTeamReturnsEmptyTeam() {
        when(teamRepository.findByAccountId(ACCOUNT_ID))
                .thenReturn(Optional.empty());

        TeamDto result =
                teamService.find(ACCOUNT_ID);

        assertThat(result.getMonsterIds())
                .isEmpty();
    }

    @Test
    void findReturnsTeamInStoredOrder() {
        Team team = team(
                SECOND_MONSTER_ID,
                FIRST_MONSTER_ID
        );

        when(teamRepository.findByAccountId(ACCOUNT_ID))
                .thenReturn(Optional.of(team));

        TeamDto result =
                teamService.find(ACCOUNT_ID);

        assertThat(result.getMonsterIds())
                .containsExactly(
                        SECOND_MONSTER_ID,
                        FIRST_MONSTER_ID
                );
    }

    @Test
    void saveCreatesOrderedTeam() {
        List<UUID> monsterIds = List.of(
                SECOND_MONSTER_ID,
                FIRST_MONSTER_ID
        );

        when(teamRepository.findByAccountId(ACCOUNT_ID))
                .thenReturn(Optional.empty());

        when(
                monsterRepository
                        .countByIdInAndAccountId(
                                Set.copyOf(monsterIds),
                                ACCOUNT_ID
                        )
        ).thenReturn(2L);

        TeamDto result = teamService.save(
                ACCOUNT_ID,
                new SaveTeamRequest(monsterIds)
        );

        ArgumentCaptor<Team> captor =
                ArgumentCaptor.forClass(Team.class);

        verify(teamRepository)
                .save(captor.capture());

        Team saved = captor.getValue();

        assertThat(saved.getAccountId())
                .isEqualTo(ACCOUNT_ID);

        assertThat(saved.getMonsterIds())
                .containsExactly(
                        SECOND_MONSTER_ID,
                        FIRST_MONSTER_ID
                );

        assertThat(result.getMonsterIds())
                .containsExactly(
                        SECOND_MONSTER_ID,
                        FIRST_MONSTER_ID
                );
    }

    @Test
    void saveReplacesExistingTeamAndOrder() {
        Team existing = team(FIRST_MONSTER_ID);

        List<UUID> replacement = List.of(
                SECOND_MONSTER_ID,
                FIRST_MONSTER_ID
        );

        when(teamRepository.findByAccountId(ACCOUNT_ID))
                .thenReturn(Optional.of(existing));

        when(
                monsterRepository
                        .countByIdInAndAccountId(
                                Set.copyOf(replacement),
                                ACCOUNT_ID
                        )
        ).thenReturn(2L);

        TeamDto result = teamService.save(
                ACCOUNT_ID,
                new SaveTeamRequest(replacement)
        );

        assertThat(existing.getMonsterIds())
                .containsExactly(
                        SECOND_MONSTER_ID,
                        FIRST_MONSTER_ID
                );

        assertThat(result.getMonsterIds())
                .containsExactly(
                        SECOND_MONSTER_ID,
                        FIRST_MONSTER_ID
                );

        verify(teamRepository).save(existing);
    }

    @Test
    void duplicateMonsterIsRejected() {
        SaveTeamRequest request =
                new SaveTeamRequest(
                        List.of(
                                FIRST_MONSTER_ID,
                                FIRST_MONSTER_ID
                        )
                );

        Throwable thrown = catchThrowable(() ->
                teamService.save(
                        ACCOUNT_ID,
                        request
                )
        );

        assertThat(thrown)
                .isInstanceOfSatisfying(
                        ApiException.class,
                        exception -> {
                            assertThat(
                                    exception.getStatusCode()
                            ).isEqualTo(
                                    HttpStatus.UNPROCESSABLE_CONTENT
                            );

                            assertThat(
                                    exception
                                            .getBody()
                                            .getProperties()
                            ).containsEntry(
                                    "code",
                                    "team.duplicate_monster"
                            );
                        }
                );

        verify(monsterRepository, never())
                .countByIdInAndAccountId(
                        any(),
                        any()
                );

        verify(teamRepository, never())
                .save(any());
    }

    @Test
    void unownedOrUnknownMonsterIsRejected() {
        List<UUID> monsterIds = List.of(
                FIRST_MONSTER_ID,
                SECOND_MONSTER_ID
        );

        when(
                monsterRepository
                        .countByIdInAndAccountId(
                                Set.copyOf(monsterIds),
                                ACCOUNT_ID
                        )
        ).thenReturn(1L);

        Throwable thrown = catchThrowable(() ->
                teamService.save(
                        ACCOUNT_ID,
                        new SaveTeamRequest(monsterIds)
                )
        );

        assertThat(thrown)
                .isInstanceOfSatisfying(
                        ApiException.class,
                        exception -> {
                            assertThat(
                                    exception.getStatusCode()
                            ).isEqualTo(
                                    HttpStatus.UNPROCESSABLE_CONTENT
                            );

                            assertThat(
                                    exception
                                            .getBody()
                                            .getProperties()
                            ).containsEntry(
                                    "code",
                                    "team.monster_not_owned"
                            );
                        }
                );

        verify(teamRepository, never())
                .save(any());
    }

    private static Team team(UUID... monsterIds) {
        Team team = new Team();
        team.setAccountId(ACCOUNT_ID);
        team.getMonsterIds().addAll(
                List.of(monsterIds)
        );
        return team;
    }
}