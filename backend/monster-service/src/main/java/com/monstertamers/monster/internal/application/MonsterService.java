package com.monstertamers.monster.internal.application;

import com.monstertamers.monster.internal.domain.Monster;
import com.monstertamers.monster.internal.error.MonsterErrorCode;
import com.monstertamers.monster.internal.persistence.MonsterRepository;
import com.monstertamers.monster.internal.web.dto.response.MonsterDto;
import com.monstertamers.monster.internal.web.mapper.MonsterMapper;
import com.monstertamers.web.error.ApiException;
import java.util.List;
import java.util.UUID;
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

    private final MonsterRepository monsterRepository;
    private final MonsterMapper monsterMapper;
    private final MonsterGenerator monsterGenerator;

    @Transactional(readOnly = true)
    public List<MonsterDto> findAll(UUID accountId) {
        return monsterRepository.findAllByAccountId(accountId).stream()
                .map(monsterMapper::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public MonsterDto findById(UUID monsterId, UUID accountId) {
        return monsterRepository.findByIdAndAccountId(monsterId, accountId)
                .map(monsterMapper::toDto)
                .orElseThrow(() -> new ApiException(MonsterErrorCode.MONSTER_NOT_FOUND));
    }

    @Transactional
    public void initCollection(UUID accountId) {

        if (monsterRepository.existsByAccountId(accountId)) {
            log.info("Account {} already has monsters, starters skipped", accountId);
            return;
        }

        List<Monster> starters = Stream.generate(monsterGenerator::generate)
                .limit(STARTER_COUNT)
                .peek(monster -> monster.setAccountId(accountId))
                .toList();

        monsterRepository.saveAll(starters);

        log.info("Account {} received {} starter monsters", accountId, starters.size());
    }
}
