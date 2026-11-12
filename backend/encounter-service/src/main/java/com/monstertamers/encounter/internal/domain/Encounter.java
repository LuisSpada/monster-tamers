package com.monstertamers.encounter.internal.domain;

import jakarta.persistence.Column;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "encounter")
@Getter
@Setter
public class Encounter {

    @Id
    private UUID id;

    @Column(name = "encounter_order", nullable = false)
    private int order;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String description;

    @Column(name = "egg_chance", nullable = false)
    private int eggChance;

    @Column(name = "is_final_boss", nullable = false)
    private boolean finalBoss;

    @ElementCollection
    @CollectionTable(
            name = "encounter_reward_class",
            joinColumns = @JoinColumn(name = "encounter_id")
    )
    @Column(name = "reward_class", nullable = false)
    private List<String> rewardClasses = new ArrayList<>();
}
