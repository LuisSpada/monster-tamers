package com.monstertamers.encounter.internal.application;

import com.monstertamers.encounter.internal.domain.Encounter;
import com.monstertamers.encounter.internal.web.dto.response.EncounterShortDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface EncounterShortMapper {

    @Mapping(target = "encounterId", source = "id")
    @Mapping(target = "encounterName", source = "name")
    EncounterShortDto toDto(Encounter encounter);
}
