package com.monstertamers.encounter.internal.domain;

import jakarta.annotation.Nullable;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Embeddable
@Getter
@Setter
public class BattleRewards {

    @Nullable
    @Column(name = "egg_reward")
    @JdbcTypeCode(SqlTypes.JSON)
    private Egg egg;
}
