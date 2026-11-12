package com.monstertamers.encounter.internal.persistence;

import com.monstertamers.encounter.internal.domain.AccountEncounterProgress;
import com.monstertamers.encounter.internal.domain.AccountEncounterProgressId;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountEncounterProgressRepository
        extends JpaRepository<AccountEncounterProgress, AccountEncounterProgressId> {

    List<AccountEncounterProgress> findAllByAccountId(UUID accountId);

    Optional<AccountEncounterProgress> findByAccountIdAndEncounterId(
            UUID accountId,
            UUID encounterId
    );
}
