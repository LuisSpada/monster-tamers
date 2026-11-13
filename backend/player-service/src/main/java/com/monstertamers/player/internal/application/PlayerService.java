package com.monstertamers.player.internal.application;

import com.monstertamers.player.api.PlayerCreatedEvent;
import com.monstertamers.player.internal.domain.Player;
import com.monstertamers.player.internal.persistence.PlayerRepository;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PlayerService {

    private final PlayerRepository playerRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public Player createIfMissing(UUID accountId) {

        Optional<Player> existing = playerRepository.findByAccountId(accountId);
        if (existing.isPresent()) {
            log.debug("Player for account {} already exists", accountId);
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
        Player saved = playerRepository.save(player);

        eventPublisher.publishEvent(new PlayerCreatedEvent(saved.getAccountId()));

        log.info("Player {} created for account {}", saved.getId(), accountId);
        return saved;
    }
}
