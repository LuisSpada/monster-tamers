package com.monstertamers.encounter.internal.mapper.rewards;

import com.monstertamers.encounter.internal.domain.BattleRewards;
import com.monstertamers.encounter.internal.web.dto.response.rewards.BattleRewardsDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = EggMapper.class)
public interface BattleRewardsMapper {

    BattleRewardsDto toDto(BattleRewards rewards);
}
