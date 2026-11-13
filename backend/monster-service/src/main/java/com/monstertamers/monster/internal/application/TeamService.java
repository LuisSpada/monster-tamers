package com.monstertamers.monster.internal.application;

import com.monstertamers.monster.internal.web.dto.response.TeamDto;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class TeamService {

    public TeamDto find(UUID accountId) {

        return TeamDto.builder()
                .monsterIds(List.of())
                .build();
    }
}
