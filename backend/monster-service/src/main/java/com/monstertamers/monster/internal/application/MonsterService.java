package com.monstertamers.monster.internal.application;

import com.monstertamers.monster.internal.error.MonsterErrorCode;
import com.monstertamers.monster.internal.persistence.MonsterRepository;
import com.monstertamers.monster.internal.web.dto.response.MonsterDto;
import com.monstertamers.monster.internal.web.mapper.MonsterMapper;
import com.monstertamers.web.error.ApiException;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MonsterService {

    private final MonsterRepository monsterRepository;
    private final MonsterMapper monsterMapper;

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
}
