package com.monstertamers.encounter.internal.domain;

import java.io.Serializable;
import java.util.UUID;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode
public class AccountEncounterProgressId implements Serializable {

    private UUID accountId;
    private UUID encounterId;

    public AccountEncounterProgressId() {
    }

    public AccountEncounterProgressId(UUID accountId, UUID encounterId) {
        this.accountId = accountId;
        this.encounterId = encounterId;
    }

    public UUID getAccountId() {
        return accountId;
    }

    public UUID getEncounterId() {
        return encounterId;
    }
}
