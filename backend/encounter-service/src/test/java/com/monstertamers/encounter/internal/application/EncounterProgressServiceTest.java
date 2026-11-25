package com.monstertamers.encounter.internal.application;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.monstertamers.encounter.internal.domain.AccountEncounterProgress;
import com.monstertamers.encounter.internal.domain.Encounter;
import com.monstertamers.encounter.internal.domain.EncounterStatus;
import com.monstertamers.encounter.internal.persistence.AccountEncounterProgressRepository;
import com.monstertamers.encounter.internal.persistence.EncounterRepository;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EncounterProgressServiceTest {

    @InjectMocks
    private EncounterProgressService service;

    @Mock
    private EncounterRepository encounterRepository;

    @Mock
    private AccountEncounterProgressRepository progressRepository;

    private final UUID tamerId = UUID.randomUUID();

    private int lastOrder = 0;

    @Test
    @DisplayName("First victory in an encounter without progress clears it and unlocks the next one")
    void advance_firstVictory_succeeds() {

        // given
        Encounter current = givenCurrentEncounter();
        Encounter next = encounter();
        givenNextEncounter(current, next);

        givenNoProgress(current);
        givenNoProgress(next);

        // when
        service.advance(tamerId, current.getId());

        // then
        thenProgressSaved(current, EncounterStatus.cleared);
        thenProgressSaved(next, EncounterStatus.available);
    }

    @Test
    @DisplayName("Victory in an unlocked encounter clears it and unlocks the next one")
    void advance_unlockedEncounter_succeeds() {

        // given
        Encounter current = givenCurrentEncounter();
        Encounter next = encounter();
        givenNextEncounter(current, next);

        givenProgress(current, EncounterStatus.available);
        givenNoProgress(next);

        // when
        service.advance(tamerId, current.getId());

        // then
        thenProgressSaved(current, EncounterStatus.cleared);
        thenProgressSaved(next, EncounterStatus.available);
    }

    @Test
    @DisplayName("Replaying a cleared encounter does not change progress")
    void advance_replay_succeeds() {

        // given
        Encounter current = givenCurrentEncounter();

        givenProgress(current, EncounterStatus.cleared);

        // when
        service.advance(tamerId, current.getId());

        // then
        thenNothingSaved();
    }

    @Test
    @DisplayName("Victory in the final encounter clears it and unlocks nothing")
    void advance_finalEncounter_succeeds() {

        // given
        Encounter current = givenCurrentEncounter();
        givenNoNextEncounter(current);

        givenProgress(current, EncounterStatus.available);

        // when
        service.advance(tamerId, current.getId());

        // then
        thenProgressSaved(current, EncounterStatus.cleared);
    }

    private Encounter givenCurrentEncounter() {

        Encounter encounter = encounter();
        when(encounterRepository.findById(encounter.getId())).thenReturn(Optional.of(encounter));
        return encounter;
    }

    private void givenNextEncounter(Encounter current, Encounter next) {

        when(encounterRepository.findFirstByOrderGreaterThanOrderByOrderAsc(current.getOrder()))
                .thenReturn(Optional.of(next));
    }

    private void givenNoNextEncounter(Encounter current) {

        when(encounterRepository.findFirstByOrderGreaterThanOrderByOrderAsc(current.getOrder()))
                .thenReturn(Optional.empty());
    }

    private void givenProgress(Encounter encounter, EncounterStatus status) {

        AccountEncounterProgress progress = new AccountEncounterProgress(tamerId, encounter.getId());
        progress.setStatus(status);
        when(progressRepository.findByAccountIdAndEncounterId(tamerId, encounter.getId()))
                .thenReturn(Optional.of(progress));
    }

    private void givenNoProgress(Encounter encounter) {

        when(progressRepository.findByAccountIdAndEncounterId(tamerId, encounter.getId()))
                .thenReturn(Optional.empty());
    }

    private void thenProgressSaved(Encounter encounter, EncounterStatus status) {

        verify(progressRepository).save(argThat(progress ->
                progress.getAccountId().equals(tamerId)
                        && progress.getEncounterId().equals(encounter.getId())
                        && progress.getStatus() == status
        ));
    }

    private void thenNothingSaved() {

        verify(progressRepository, never()).save(any());
    }

    private Encounter encounter() {

        Encounter encounter = new Encounter();
        encounter.setId(UUID.randomUUID());
        encounter.setOrder(++lastOrder);
        return encounter;
    }
}
