package com.monstertamers.monster.internal.application;

import com.monstertamers.monster.internal.domain.Monster;
import com.monstertamers.monster.internal.error.MonsterErrorCode;
import com.monstertamers.monster.internal.persistence.MonsterRepository;
import com.monstertamers.monster.internal.web.dto.response.MonsterDto;
import com.monstertamers.monster.internal.web.mapper.MonsterMapper;
import com.monstertamers.web.error.ApiException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class MonsterService {

    private static final int STARTER_COUNT = 3;
    private static final int MAX_BATTLE_TEAM_SIZE = 3;

    private final MonsterRepository monsterRepository;
    private final MonsterMapper monsterMapper;
    private final MonsterGenerator monsterGenerator;

    @Transactional(readOnly = true)
    public List<MonsterDto> findAll(
            UUID accountId
    ) {
        return monsterRepository
                .findAllByAccountId(accountId)
                .stream()
                .map(monsterMapper::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public MonsterDto findById(
            UUID monsterId,
            UUID accountId
    ) {
        return monsterRepository
                .findByIdAndAccountId(
                        monsterId,
                        accountId
                )
                .map(monsterMapper::toDto)
                .orElseThrow(() ->
                        new ApiException(
                                MonsterErrorCode
                                        .MONSTER_NOT_FOUND
                        )
                );
    }

    @Transactional(readOnly = true)
    public List<MonsterDto> findOwnedForBattle(
            UUID accountId,
            List<UUID> monsterIds
    ) {
        validateBattleMonsterIds(monsterIds);

        Map<UUID, Monster> monstersById =
                monsterRepository
                        .findAllByIdInAndAccountId(
                                monsterIds,
                                accountId
                        )
                        .stream()
                        .collect(
                                Collectors.toMap(
                                        Monster::getId,
                                        monster -> monster
                                )
                        );

        if (
                monstersById.size()
                        != monsterIds.size()
        ) {
            throw new ApiException(
                    MonsterErrorCode
                            .TEAM_MONSTER_NOT_OWNED
            );
        }

        return monsterIds
                .stream()
                .map(monstersById::get)
                .map(monsterMapper::toDto)
                .toList();
    }

    @Transactional
    public void initCollection(
            UUID accountId
    ) {
        if (
                monsterRepository
                        .existsByAccountId(accountId)
        ) {
            log.info(
                    "Account {} already has monsters, starters skipped",
                    accountId
            );

            return;
        }

        List<Monster> starters =
                Stream.generate(
                                monsterGenerator::generate
                        )
                        .limit(STARTER_COUNT)
                        .peek(monster ->
                                monster.setAccountId(
                                        accountId
                                )
                        )
                        .toList();

        monsterRepository.saveAll(starters);

        log.info(
                "Account {} received {} starter monsters",
                accountId,
                starters.size()
        );
    }

    private void validateBattleMonsterIds(
            List<UUID> monsterIds
    ) {
        if (
                monsterIds == null
                        || monsterIds.isEmpty()
                        || monsterIds.size()
                        > MAX_BATTLE_TEAM_SIZE
                        || monsterIds.stream()
                        .anyMatch(id -> id == null)
        ) {
            throw new ApiException(
                    MonsterErrorCode
                            .TEAM_MONSTER_NOT_OWNED
            );
        }

        long uniqueIdCount =
                monsterIds
                        .stream()
                        .distinct()
                        .count();

        if (uniqueIdCount != monsterIds.size()) {
            throw new ApiException(
                    MonsterErrorCode
                            .TEAM_DUPLICATE_MONSTER
            );
        }
    }
}