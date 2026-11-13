package com.monstertamers.player.internal.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

import com.monstertamers.player.api.PlayerCreatedEvent;
import com.monstertamers.player.internal.domain.Player;
import com.monstertamers.player.internal.persistence.PlayerRepository;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(MockitoExtension.class)
class PlayerServiceTest {

    @Mock
    private PlayerRepository playerRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private PlayerService playerService;

    @Test
    void createIfMissing_existingAccount_returnsExistingPlayer() {
        UUID accountId = UUID.randomUUID();
        Player existing = player(accountId, "Tamer-1234");
        when(playerRepository.findByAccountId(accountId)).thenReturn(Optional.of(existing));

        Player result = playerService.createIfMissing(accountId);

        assertThat(result).isSameAs(existing);
        verify(playerRepository).findByAccountId(accountId);
        verify(eventPublisher, never()).publishEvent(any(Object.class));
    }

    @Test
    void createIfMissing_missingAccount_savesPlayerWithGeneratedNickname() {
        UUID accountId = UUID.randomUUID();
        when(playerRepository.findByAccountId(accountId)).thenReturn(Optional.empty());
        when(playerRepository.existsByNickname(org.mockito.ArgumentMatchers.anyString()))
                .thenReturn(false);
        when(playerRepository.save(org.mockito.ArgumentMatchers.any(Player.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Player result = playerService.createIfMissing(accountId);

        assertThat(result.getAccountId()).isEqualTo(accountId);
        assertThat(result.getNickname()).matches("Tamer-\\d{4}");
        verify(playerRepository).save(result);
        verify(eventPublisher).publishEvent(new PlayerCreatedEvent(accountId));
    }

    @Test
    void createIfMissing_takenNickname_generatesAnotherNickname() {
        UUID accountId = UUID.randomUUID();
        when(playerRepository.findByAccountId(accountId)).thenReturn(Optional.empty());
        when(playerRepository.existsByNickname(org.mockito.ArgumentMatchers.anyString()))
                .thenReturn(true, false);
        when(playerRepository.save(org.mockito.ArgumentMatchers.any(Player.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Player result = playerService.createIfMissing(accountId);

        assertThat(result.getNickname()).matches("Tamer-\\d{4}");
        verify(playerRepository, times(2))
                .existsByNickname(org.mockito.ArgumentMatchers.anyString());
        verify(playerRepository).save(result);
    }

    private Player player(UUID accountId, String nickname) {
        Player player = new Player();
        player.setAccountId(accountId);
        player.setNickname(nickname);
        return player;
    }
}
