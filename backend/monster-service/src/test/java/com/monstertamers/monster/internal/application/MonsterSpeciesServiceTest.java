package com.monstertamers.monster.internal.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.monstertamers.monster.internal.domain.MonsterClass;
import com.monstertamers.monster.internal.domain.MonsterSpecies;
import com.monstertamers.monster.internal.persistence.MonsterSpeciesRepository;
import com.monstertamers.monster.internal.web.dto.response.MonsterSpeciesDto;
import com.monstertamers.monster.internal.web.mapper.MonsterMapper;
import com.monstertamers.web.error.ApiException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

@ExtendWith(MockitoExtension.class)
class MonsterSpeciesServiceTest {

    private static final UUID FOX_ID = UUID.fromString("20000000-0000-0000-0000-000000000001");
    private static final UUID UNKNOWN_ID = UUID.fromString("99999999-9999-9999-9999-999999999999");

    @Mock
    private MonsterSpeciesRepository monsterSpeciesRepository;

    @Mock
    private MonsterMapper monsterMapper;

    @InjectMocks
    private MonsterSpeciesService monsterSpeciesService;

    @Test
    void findAllMapsOrderedSpecies() {
        MonsterSpecies fox = new MonsterSpecies();
        fox.setId(FOX_ID);
        fox.setName("Fierce Fox");
        fox.setMonsterClass(MonsterClass.FIGHTER);

        MonsterSpeciesDto expected = MonsterSpeciesDto.builder()
                .id(FOX_ID)
                .name("Fierce Fox")
                .monsterClass(MonsterClass.FIGHTER)
                .build();

        when(monsterSpeciesRepository.findAllByOrderByNameAsc()).thenReturn(List.of(fox));
        when(monsterMapper.toSpeciesDto(fox)).thenReturn(expected);

        List<MonsterSpeciesDto> result = monsterSpeciesService.findAll();

        assertThat(result).containsExactly(expected);
    }

    @Test
    void findByIdThrowsNotFoundWhenMissing() {
        when(monsterSpeciesRepository.findById(UNKNOWN_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> monsterSpeciesService.findById(UNKNOWN_ID))
                .isInstanceOf(ApiException.class)
                .satisfies(ex -> assertThat(((ApiException) ex).getStatusCode())
                        .isEqualTo(HttpStatus.NOT_FOUND));
    }
}