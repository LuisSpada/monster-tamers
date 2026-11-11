package com.monstertamers.player.internal.web.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.monstertamers.player.internal.application.PlayerService;
import com.monstertamers.player.internal.domain.Player;
import com.monstertamers.player.internal.web.dto.response.PlayerProfileResponse;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.jwt.Jwt;

@ExtendWith(MockitoExtension.class)
public class PlayerControllerTest {

    @Mock
    private PlayerService playerService;

    @Mock
    private Jwt jwt;

    @InjectMocks
    private PlayerController playerController;

    @Test
    void me_usesJwtSubjectAndReturnsPlayerProfile() {
        UUID accountId = UUID.randomUUID();
        UUID playerId = UUID.randomUUID();
        Player player = player(playerId, accountId, "Tamer-1234");
        when(jwt.getSubject()).thenReturn(accountId.toString());
        when(playerService.createIfMissing(accountId)).thenReturn(player);

        PlayerProfileResponse response = playerController.me(jwt);

        assertThat(response).isEqualTo(
                new PlayerProfileResponse(playerId, "Tamer-1234"));
        verify(playerService).createIfMissing(accountId);
    }

    @Test
    void me_rejectsInvalidJwtSubject() {
        when(jwt.getSubject()).thenReturn("not-a-uuid");

        assertThatThrownBy(() -> playerController.me(jwt))
                .isInstanceOf(IllegalArgumentException.class);
    }

    private Player player(UUID id, UUID accountId, String nickname) {
        Player player = new Player();
        player.setId(id);
        player.setAccountId(accountId);
        player.setNickname(nickname);
        return player;
    }
}
