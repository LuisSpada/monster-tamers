package com.monstertamers.monster.internal.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.monstertamers.monster.internal.domain.Monster;
import com.monstertamers.monster.internal.persistence.MonsterRepository;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MonsterServiceTest {

    @Mock
    private MonsterRepository monsterRepository;

    @Mock
    private MonsterGenerator monsterGenerator;

    @InjectMocks
    private MonsterService monsterService;

    @Captor
    private ArgumentCaptor<List<Monster>> savedCaptor;

    @Test
    @DisplayName("initCollection skips an account that already has monsters")
    void initCollection_existingMonsters_skipped() {

        // given
        UUID accountId = UUID.randomUUID();
        when(monsterRepository.existsByAccountId(accountId)).thenReturn(true);

        // when
        monsterService.initCollection(accountId);

        // then
        verify(monsterRepository, never()).saveAll(any());
    }

    @Test
    @DisplayName("initCollection gives an empty collection three monsters owned by the account")
    void initCollection_emptyCollection_succeeds() {

        // given
        UUID accountId = UUID.randomUUID();
        when(monsterRepository.existsByAccountId(accountId)).thenReturn(false);
        when(monsterGenerator.generate()).thenAnswer(invocation -> new Monster());

        // when
        monsterService.initCollection(accountId);

        // then
        verify(monsterRepository).saveAll(savedCaptor.capture());
        assertThat(savedCaptor.getValue())
                .hasSize(3)
                .allSatisfy(monster -> assertThat(monster.getAccountId()).isEqualTo(accountId));
    }
}
