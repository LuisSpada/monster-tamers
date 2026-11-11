package com.monstertamers.player.internal.persistence;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.monstertamers.player.TestcontainersConfiguration;
import com.monstertamers.player.internal.domain.Player;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(properties = {
        "app.jwt.secret=test-secret-at-least-32-bytes-long"
})
@Import(TestcontainersConfiguration.class)
@Transactional
class PlayerRepositoryTest {

    @Autowired
    private PlayerRepository playerRepository;

    @Test
    void save_takenAccount_rejected() {
        UUID accountId = UUID.randomUUID();
        playerRepository.saveAndFlush(player(accountId, "Tamer-0001"));

        assertThatThrownBy(() -> playerRepository.saveAndFlush(player(accountId, "Tamer-0002")))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void save_takenNickname_rejected() {
        playerRepository.saveAndFlush(player(UUID.randomUUID(), "Tamer-0001"));

        assertThatThrownBy(() -> playerRepository.saveAndFlush(player(UUID.randomUUID(), "Tamer-0001")))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    private Player player(UUID accountId, String nickname) {
        Player player = new Player();
        player.setAccountId(accountId);
        player.setNickname(nickname);
        return player;
    }
}
