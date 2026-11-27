package com.monstertamers.monster.internal.application;

import com.monstertamers.monster.internal.error.MonsterErrorCode;
import com.monstertamers.monster.internal.persistence.MonsterSpeciesRepository;
import com.monstertamers.monster.internal.web.dto.response.MonsterSpeciesDto;
import com.monstertamers.monster.internal.web.mapper.MonsterMapper;
import com.monstertamers.web.error.ApiException;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MonsterSpeciesService {

    private final MonsterSpeciesRepository monsterSpeciesRepository;
    private final MonsterMapper monsterMapper;

    @Transactional(readOnly = true)
    public List<MonsterSpeciesDto> findAll() {
        return monsterSpeciesRepository
                .findAllByOrderByNameAsc()
                .stream()
                .map(monsterMapper::toSpeciesDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public MonsterSpeciesDto findById(UUID speciesId) {
        return monsterSpeciesRepository
                .findById(speciesId)
                .map(monsterMapper::toSpeciesDto)
                .orElseThrow(() -> new ApiException(MonsterErrorCode.SPECIES_NOT_FOUND));
    }
}