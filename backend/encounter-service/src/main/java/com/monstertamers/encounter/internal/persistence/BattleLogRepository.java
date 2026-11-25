package com.monstertamers.encounter.internal.persistence;

import com.monstertamers.encounter.internal.domain.BattleLog;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BattleLogRepository extends JpaRepository<BattleLog, UUID> {
}
