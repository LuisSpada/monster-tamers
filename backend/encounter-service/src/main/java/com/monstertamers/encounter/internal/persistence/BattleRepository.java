package com.monstertamers.encounter.internal.persistence;

import com.monstertamers.encounter.internal.domain.Battle;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BattleRepository extends JpaRepository<Battle, UUID> {

    @EntityGraph(attributePaths = "encounter")
    List<Battle> findAllByAccountIdOrderByPlayedAtDesc(UUID accountId);

    @EntityGraph(attributePaths = "encounter")
    Optional<Battle> findByIdAndAccountId(UUID id, UUID accountId);
}
