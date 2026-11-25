package com.monstertamers.encounter.internal.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "account_encounter_progress")
@IdClass(AccountEncounterProgressId.class)
@Getter
@Setter
@NoArgsConstructor
public class AccountEncounterProgress {

    @Id
    @Column(name = "account_id", nullable = false)
    private UUID accountId;

    @Id
    @Column(name = "encounter_id", nullable = false)
    private UUID encounterId;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private EncounterStatus status;

    public AccountEncounterProgress(UUID accountId, UUID encounterId) {

        this.accountId = accountId;
        this.encounterId = encounterId;
        this.status = EncounterStatus.available;
    }

}
