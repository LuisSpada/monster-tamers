package com.monstertamers.monster.internal.domain;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
@Entity
@Table(name = "monster_species")
@Getter
@Setter
public class MonsterSpecies {
    @Id
    @Column(name = "id", nullable = false)
    private UUID id;
    @Column(name = "name", nullable = false, unique = true)
    private String name;
    @Enumerated(EnumType.STRING)
    @Column(name = "monster_class", nullable = false)
    private MonsterClass monsterClass;
    @Column(name = "base_strength", nullable = false)
    private int baseStrength;
    @Column(name = "base_hp", nullable = false)
    private int baseHp;
    @Column(name = "base_accuracy", nullable = false)
    private int baseAccuracy;
    @Column(name = "base_agility", nullable = false)
    private int baseAgility;
    @Column(name = "skill_name", nullable = false)
    private String skillName;
    @Column(name = "skill_description", nullable = false)
    private String skillDescription;
    @Column(name = "trait_name", nullable = false)
    private String traitName;
    @Column(name = "trait_description", nullable = false)
    private String traitDescription;
    @Column(name = "avatar_variant", nullable = false)
    private int avatarVariant;
}