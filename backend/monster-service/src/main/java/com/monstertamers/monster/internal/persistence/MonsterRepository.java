package com.monstertamers.monster.internal.persistence;

import com.monstertamers.monster.internal.domain.Monster;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MonsterRepository
        extends JpaRepository<Monster, UUID> {

    List<Monster> findAllByAccountId(
            UUID accountId
    );

    Optional<Monster> findByIdAndAccountId(
            UUID id,
            UUID accountId
    );

    List<Monster> findAllByIdInAndAccountId(
            Collection<UUID> ids,
            UUID accountId
    );

    boolean existsByAccountId(
            UUID accountId
    );

    long countByIdInAndAccountId(
            Collection<UUID> ids,
            UUID accountId
    );
}