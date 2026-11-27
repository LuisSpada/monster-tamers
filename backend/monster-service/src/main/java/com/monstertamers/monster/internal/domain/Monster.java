package com.monstertamers.monster.internal.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "monster")
@Getter
@Setter
public class Monster {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "account_id", nullable = false)
    private UUID accountId;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "species_id", nullable = false)
    private MonsterSpecies species;

    @Column(name = "nickname")
    private String nickname;

    @Column(name = "level", nullable = false)
    private int level;

    @Column(name = "xp", nullable = false)
    private int xp;

    @Column(name = "talent", nullable = false)
    private int talent;

    @Column(name = "pending_points", nullable = false)
    private int pendingPoints;

    @Column(name = "strength", nullable = false)
    private int strength;

    @Column(name = "hp", nullable = false)
    private int hp;

    @Column(name = "accuracy", nullable = false)
    private int accuracy;

    @Column(name = "agility", nullable = false)
    private int agility;

    @Column(name = "avatar_variant", nullable = false)
    private int avatarVariant;

    @Column(name = "obtained_at", nullable = false, insertable = false, updatable = false)
    private Instant obtainedAt;

    public String getDisplayName() {
        return (nickname != null && !nickname.isBlank()) ? nickname : species.getName();
    }
}