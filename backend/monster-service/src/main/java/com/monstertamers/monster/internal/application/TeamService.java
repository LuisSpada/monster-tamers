package com.monstertamers.monster.internal.application;

import com.monstertamers.monster.internal.domain.Team;
import com.monstertamers.monster.internal.persistence.TeamRepository;
import com.monstertamers.monster.internal.web.dto.response.TeamDto;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TeamService {

    private final TeamRepository teamRepository;

    @Transactional(readOnly = true)
    public TeamDto find(UUID accountId) {

        List<UUID> monsterIds = teamRepository.findByAccountId(accountId)
                .map(Team::getMonsterIds)
                .map(List::copyOf)
                .orElse(List.of());

        return TeamDto.builder()
                .monsterIds(monsterIds)
                .build();
    }
}
