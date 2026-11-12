package com.monstertamers.encounter.internal.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "encounter_opponent")
@Getter
@Setter
public class EncounterOpponent {

    @Id
    private UUID id;

    @Column(name = "encounter_id", nullable = false)
    private UUID encounterId;

    @Column(nullable = false)
    private String name;

    @Column(name = "monster_class", nullable = false)
    private String monsterClass;

    @Column(nullable = false)
    private int level;

    @Column(name = "avatar_variant", nullable = false)
    private int avatarVariant;
}
