package com.monstertamers.player.internal.application;

import com.monstertamers.player.internal.domain.Player;
import com.monstertamers.player.internal.persistence.PlayerRepository;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PlayerService {

    private final PlayerRepository playerRepository;

    public PlayerService(PlayerRepository playerRepository) {
        this.playerRepository = playerRepository;
    }

    @Transactional
    public Player createIfMissing(UUID accountId) {
        Optional<Player> existing = playerRepository.findByAccountId(accountId);
        if (existing.isPresent()) {
            return existing.get();
        }

        String nickname;
        do {
            int number = ThreadLocalRandom.current().nextInt(1000, 10000);
            nickname = "Tamer-" + number;
        } while (playerRepository.existsByNickname(nickname));

        Player player = new Player();
        player.setAccountId(accountId);
        player.setNickname(nickname);
        return playerRepository.save(player);
    }
}
