package com.monstertamers.encounter.internal.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.util.List;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "battle_log")
@Getter
@Setter
public class BattleLog {

    @Id
    private UUID battleId;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "battle_id")
    private Battle battle;

    @Column(nullable = false)
    @JdbcTypeCode(SqlTypes.JSON)
    private List<BattleLogEntry> events;
}
