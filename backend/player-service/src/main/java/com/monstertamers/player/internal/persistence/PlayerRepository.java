package com.monstertamers.player.internal.persistence;

import com.monstertamers.player.internal.domain.Player;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlayerRepository extends JpaRepository<Player, UUID> {
}
