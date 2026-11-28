package com.monstertamers.monster.internal.persistence;

import com.monstertamers.monster.internal.domain.MonsterClass;
import com.monstertamers.monster.internal.domain.MonsterSpecies;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MonsterSpeciesRepository extends JpaRepository<MonsterSpecies, UUID> {

    List<MonsterSpecies> findAllByOrderByNameAsc();

    List<MonsterSpecies> findAllByMonsterClass(MonsterClass monsterClass);
}