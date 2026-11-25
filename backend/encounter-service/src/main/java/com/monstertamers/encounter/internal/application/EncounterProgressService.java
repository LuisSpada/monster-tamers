package com.monstertamers.encounter.internal.application;

import com.monstertamers.encounter.internal.domain.AccountEncounterProgress;
import com.monstertamers.encounter.internal.domain.Encounter;
import com.monstertamers.encounter.internal.domain.EncounterStatus;
import com.monstertamers.encounter.internal.error.EncounterErrorCode;
import com.monstertamers.encounter.internal.persistence.AccountEncounterProgressRepository;
import com.monstertamers.encounter.internal.persistence.EncounterRepository;
import com.monstertamers.web.error.ApiException;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class EncounterProgressService {

    private final EncounterRepository encounterRepository;
    private final AccountEncounterProgressRepository progressRepository;

    @Transactional
    public void advance(UUID accountId, UUID encounterId) {

        Encounter current = encounterRepository
                .findById(encounterId)
                .orElseThrow(() -> new ApiException(EncounterErrorCode.ENCOUNTER_NOT_FOUND));

        AccountEncounterProgress currentProgress = findOrCreate(accountId, encounterId);
        if (currentProgress.getStatus() == EncounterStatus.cleared) {
            log.debug("Account {} replayed already cleared encounter {}", accountId, encounterId);
            return;
        }
        currentProgress.setStatus(EncounterStatus.cleared);
        progressRepository.save(currentProgress);

        Optional<Encounter> nextOptional = findNext(current);
        if (nextOptional.isEmpty()) {
            log.info("Account {} cleared the final encounter {}", accountId, encounterId);
            return;
        }

        Encounter next = nextOptional.get();
        AccountEncounterProgress nextProgress = findOrCreate(accountId, next.getId());
        nextProgress.setStatus(EncounterStatus.available);
        progressRepository.save(nextProgress);

        log.info("Account {} cleared encounter {} and unlocked {}", accountId, encounterId, next.getId());
    }

    private AccountEncounterProgress findOrCreate(UUID accountId, UUID encounterId) {

        return progressRepository
            .findByAccountIdAndEncounterId(accountId, encounterId)
            .orElseGet(() -> new AccountEncounterProgress(accountId, encounterId));
    }

    private Optional<Encounter> findNext(Encounter current) {

        return encounterRepository.findFirstByOrderGreaterThanOrderByOrderAsc(current.getOrder());
    }
}
