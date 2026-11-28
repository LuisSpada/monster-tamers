package com.monstertamers.encounter.internal.mapper.rewards;

import com.monstertamers.encounter.internal.domain.Egg;
import com.monstertamers.encounter.internal.integration.monster.response.AddedMonsterDto;
import com.monstertamers.encounter.internal.web.dto.response.rewards.EggDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface EggMapper {

    @Mapping(target = "id", source = "monsterId")
    EggDto toDto(Egg egg);

    @Mapping(target = "monsterId", source = "id")
    Egg toEgg(AddedMonsterDto monster);
}
