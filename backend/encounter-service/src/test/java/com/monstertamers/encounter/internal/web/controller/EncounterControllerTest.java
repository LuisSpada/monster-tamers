package com.monstertamers.encounter.internal.web.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.monstertamers.encounter.internal.application.EncounterService;
import com.monstertamers.encounter.internal.domain.EncounterStatus;
import com.monstertamers.encounter.internal.web.dto.response.EncounterDto;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;

class EncounterControllerTest {

    private static final UUID ACCOUNT_ID =
            UUID.fromString(
                    "32923c1a-e469-4014-b957-3e56f88a9a10"
            );

    private static final UUID ENCOUNTER_ID =
            UUID.fromString(
                    "00000000-0000-0000-0000-000000000001"
            );

    private final EncounterService encounterService =
            mock(EncounterService.class);

    private final EncounterController controller =
            new EncounterController(encounterService);

    @Test
    void listUsesAccountIdFromJwtSubject() {
        Jwt jwt = jwt();

        EncounterDto expected = encounterDto();

        when(
                encounterService.findAll(ACCOUNT_ID)
        ).thenReturn(List.of(expected));

        List<EncounterDto> result =
                controller.list(jwt);

        assertThat(result)
                .containsExactly(expected);

        verify(encounterService)
                .findAll(ACCOUNT_ID);
    }

    @Test
    void getUsesAccountIdFromJwtSubject() {
        Jwt jwt = jwt();

        EncounterDto expected = encounterDto();

        when(
                encounterService.findById(
                        ACCOUNT_ID,
                        ENCOUNTER_ID
                )
        ).thenReturn(expected);

        EncounterDto result =
                controller.get(
                        jwt,
                        ENCOUNTER_ID
                );

        assertThat(result).isSameAs(expected);

        verify(encounterService).findById(
                ACCOUNT_ID,
                ENCOUNTER_ID
        );
    }

    private static Jwt jwt() {
        Jwt jwt = mock(Jwt.class);

        when(jwt.getSubject())
                .thenReturn(ACCOUNT_ID.toString());

        return jwt;
    }

    private static EncounterDto encounterDto() {
        return new EncounterDto(
                ENCOUNTER_ID,
                1,
                "Meadow Scouts",
                "A gentle first encounter.",
                20,
                List.of(
                        "Fighter",
                        "Trickster"
                ),
                false,
                EncounterStatus.available,
                List.of()
        );
    }
}