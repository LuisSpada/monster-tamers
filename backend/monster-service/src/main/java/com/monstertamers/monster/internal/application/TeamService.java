package com.monstertamers.monster.internal.application;

import com.monstertamers.monster.internal.domain.Team;
import com.monstertamers.monster.internal.error.MonsterErrorCode;
import com.monstertamers.monster.internal.persistence.MonsterRepository;
import com.monstertamers.monster.internal.persistence.TeamRepository;
import com.monstertamers.monster.internal.web.dto.request.SaveTeamRequest;
import com.monstertamers.monster.internal.web.dto.response.TeamDto;
import com.monstertamers.web.error.ApiException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TeamService {

    private final TeamRepository teamRepository;
    private final MonsterRepository monsterRepository;

    @Transactional(readOnly = true)
    public TeamDto find(UUID accountId) {

        List<UUID> monsterIds = teamRepository.findByAccountId(accountId)
                .map(Team::getMonsterIds)
                .orElse(List.of());

        return toDto(monsterIds);
    }

    @Transactional
    public TeamDto save(UUID accountId, SaveTeamRequest request) {

        List<UUID> monsterIds = request.monsterIds();
        Set<UUID> uniqueIds = new HashSet<>(monsterIds);

        if (uniqueIds.size() != monsterIds.size()) {
            throw new ApiException(MonsterErrorCode.TEAM_DUPLICATE_MONSTER);
        }

        if (monsterRepository.countByIdInAndAccountId(uniqueIds, accountId) != uniqueIds.size()) {
            throw new ApiException(MonsterErrorCode.TEAM_MONSTER_NOT_OWNED);
        }

        Team team = teamRepository.findByAccountId(accountId)
                .orElseGet(() -> newTeam(accountId));

        team.getMonsterIds().clear();
        team.getMonsterIds().addAll(monsterIds);
        teamRepository.save(team);

        return toDto(team.getMonsterIds());
    }

    private Team newTeam(UUID accountId) {
        Team team = new Team();
        team.setAccountId(accountId);
        return team;
    }

    private TeamDto toDto(List<UUID> monsterIds) {
        return TeamDto.builder()
                .monsterIds(List.copyOf(monsterIds))
                .build();
    }
}
