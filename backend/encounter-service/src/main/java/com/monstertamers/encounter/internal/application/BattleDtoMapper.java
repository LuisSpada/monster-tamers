package com.monstertamers.encounter.internal.application;

import com.monstertamers.encounter.internal.domain.Battle;
import com.monstertamers.encounter.internal.domain.BattleLogEntry;
import com.monstertamers.encounter.internal.domain.BattleParticipant;
import com.monstertamers.encounter.internal.web.dto.response.BattleResultDto;
import com.monstertamers.encounter.internal.web.dto.response.BattleSummaryDto;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = EncounterShortMapper.class)
public interface BattleDtoMapper {

    @Mapping(target = "participantNames", source = "participants")
    @Mapping(target = "eggHatched", constant = "false")
    BattleSummaryDto toSummaryDto(Battle battle);

    BattleResultDto toResultDto(Battle battle, List<BattleLogEntry> events);

    default String toParticipantName(BattleParticipant participant) {
        return participant.name();
    }
}
