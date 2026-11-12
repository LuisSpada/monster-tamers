package com.monstertamers.encounter.internal.persistence;

import com.monstertamers.encounter.internal.domain.EncounterOpponent;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EncounterOpponentRepository extends JpaRepository<EncounterOpponent, UUID> {

    List<EncounterOpponent> findAllByEncounterId(UUID encounterId);
}
