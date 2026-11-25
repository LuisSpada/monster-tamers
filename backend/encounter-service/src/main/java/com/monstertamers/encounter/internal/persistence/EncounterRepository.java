package com.monstertamers.encounter.internal.persistence;

import com.monstertamers.encounter.internal.domain.Encounter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EncounterRepository extends JpaRepository<Encounter, UUID> {

    List<Encounter> findAllByOrderByOrderAsc();

    Optional<Encounter> findFirstByOrderGreaterThanOrderByOrderAsc(int order);
}
