package com.monstertamers.monster.internal.persistence;

import com.monstertamers.monster.internal.domain.Team;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeamRepository extends JpaRepository<Team, UUID> {

    Optional<Team> findByAccountId(UUID accountId);
}
