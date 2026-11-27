package com.monstertamers.monster.internal.web.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.monstertamers.monster.internal.application.MonsterSpeciesService;
import com.monstertamers.monster.internal.domain.MonsterClass;
import com.monstertamers.monster.internal.web.dto.response.MonsterSpeciesDto;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class MonsterSpeciesControllerTest {

    private static final UUID FOX_ID = UUID.fromString("20000000-0000-0000-0000-000000000001");

    private final MonsterSpeciesService service = mock(MonsterSpeciesService.class);
    private final MonsterSpeciesController controller = new MonsterSpeciesController(service);

    @Test
    void listDelegatesToService() {
        MonsterSpeciesDto fox = MonsterSpeciesDto.builder()
                .id(FOX_ID)
                .name("Fierce Fox")
                .monsterClass(MonsterClass.FIGHTER)
                .build();
        when(service.findAll()).thenReturn(List.of(fox));

        List<MonsterSpeciesDto> result = controller.list();

        assertThat(result).containsExactly(fox);
        verify(service).findAll();
    }

    @Test
    void getDelegatesToService() {
        MonsterSpeciesDto fox = MonsterSpeciesDto.builder()
                .id(FOX_ID)
                .name("Fierce Fox")
                .monsterClass(MonsterClass.FIGHTER)
                .build();
        when(service.findById(FOX_ID)).thenReturn(fox);

        MonsterSpeciesDto result = controller.get(FOX_ID);

        assertThat(result).isSameAs(fox);
        verify(service).findById(FOX_ID);
    }
}