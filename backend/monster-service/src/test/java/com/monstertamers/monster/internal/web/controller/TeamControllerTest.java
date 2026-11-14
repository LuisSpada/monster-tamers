package com.monstertamers.monster.internal.web.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.monstertamers.monster.internal.application.TeamService;
import com.monstertamers.monster.internal.web.dto.request.SaveTeamRequest;
import com.monstertamers.monster.internal.web.dto.response.TeamDto;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;

class TeamControllerTest {

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

    private final TeamService teamService =
            mock(TeamService.class);

    private final TeamController controller =
            new TeamController(teamService);

    @Test
    void getUsesAccountIdFromJwtSubject() {
        Jwt jwt = jwt();

        TeamDto expected = TeamDto.builder()
                .monsterIds(
                        List.of(FIRST_MONSTER_ID)
                )
                .build();

        when(teamService.find(ACCOUNT_ID))
                .thenReturn(expected);

        TeamDto result = controller.get(jwt);

        assertThat(result).isSameAs(expected);
        verify(teamService).find(ACCOUNT_ID);
    }

    @Test
    void saveUsesAccountIdFromJwtSubject() {
        Jwt jwt = jwt();

        SaveTeamRequest request =
                new SaveTeamRequest(
                        List.of(
                                SECOND_MONSTER_ID,
                                FIRST_MONSTER_ID
                        )
                );

        TeamDto expected = TeamDto.builder()
                .monsterIds(
                        List.of(
                                SECOND_MONSTER_ID,
                                FIRST_MONSTER_ID
                        )
                )
                .build();

        when(
                teamService.save(
                        ACCOUNT_ID,
                        request
                )
        ).thenReturn(expected);

        TeamDto result =
                controller.save(jwt, request);

        assertThat(result).isSameAs(expected);

        verify(teamService).save(
                ACCOUNT_ID,
                request
        );
    }

    private static Jwt jwt() {
        Jwt jwt = mock(Jwt.class);

        when(jwt.getSubject())
                .thenReturn(ACCOUNT_ID.toString());

        return jwt;
    }
}