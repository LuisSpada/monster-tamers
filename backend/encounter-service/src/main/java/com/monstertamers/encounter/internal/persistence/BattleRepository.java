package com.monstertamers.encounter.internal.persistence;

import com.monstertamers.encounter.internal.domain.Battle;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BattleRepository extends JpaRepository<Battle, UUID> {
}
