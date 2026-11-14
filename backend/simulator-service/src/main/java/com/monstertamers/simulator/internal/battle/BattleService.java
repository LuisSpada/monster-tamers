package com.monstertamers.simulator.internal.battle;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class BattleService {

    private static final int MAX_ROUNDS = 10;

    public BattleResponse simulate(
            BattleRequest request
    ) {
        validateUniqueTeam(request.team());

        long seed = battleSeed(request);

        List<Combatant> players = request.team()
                .stream()
                .map(this::toPlayerCombatant)
                .toList();

        List<Combatant> enemies = request.encounter()
                .opponents()
                .stream()
                .map(this::toEnemyCombatant)
                .toList();

        List<BattleEvent> events = new ArrayList<>();

        int completedRounds = 0;

        for (
                int round = 1;
                round <= MAX_ROUNDS;
                round++
        ) {
            completedRounds = round;

            runSide(
                    round,
                    "player",
                    players,
                    enemies,
                    seed,
                    events
            );

            if (allDefeated(enemies)) {
                break;
            }

            runSide(
                    round,
                    "enemy",
                    enemies,
                    players,
                    seed ^ 0x9E3779B97F4A7C15L,
                    events
            );

            if (allDefeated(players)) {
                break;
            }
        }

        String outcome = determineOutcome(
                players,
                enemies
        );

        events.add(new BattleEvent(
                eventId(seed, completedRounds, events.size()),
                completedRounds,
                "player",
                "info",
                outcome.equals("victory")
                        ? "Your team won the battle."
                        : "Your team was defeated."
        ));

        List<BattleParticipant> participants =
                request.team()
                        .stream()
                        .map(monster ->
                                new BattleParticipant(
                                        monster.id(),
                                        monster.name(),
                                        monster.monsterClass(),
                                        monster.level()
                                )
                        )
                        .toList();

        return new BattleResponse(
                deterministicUuid(
                        "battle:" + seed
                ),
                request.encounter().id(),
                request.encounter().name(),
                request.encounter().finalBoss(),
                deterministicPlayedAt(seed),
                outcome,
                completedRounds,
                participants,
                List.copyOf(events)
        );
    }

    private void validateUniqueTeam(
            List<BattleRequest.MonsterInput> team
    ) {
        Set<UUID> ids = new HashSet<>();

        for (BattleRequest.MonsterInput monster : team) {
            if (!ids.add(monster.id())) {
                throw new IllegalArgumentException(
                        "Battle team contains duplicate monster IDs"
                );
            }
        }
    }

    private void runSide(
            int round,
            String side,
            List<Combatant> attackers,
            List<Combatant> defenders,
            long seed,
            List<BattleEvent> events
    ) {
        for (
                int attackerIndex = 0;
                attackerIndex < attackers.size();
                attackerIndex++
        ) {
            Combatant attacker =
                    attackers.get(attackerIndex);

            if (attacker.defeated()) {
                continue;
            }

            Combatant defender =
                    firstActive(defenders);

            if (defender == null) {
                return;
            }

            int damage = calculateDamage(
                    attacker,
                    defender,
                    round,
                    attackerIndex,
                    seed
            );

            defender.takeDamage(damage);

            events.add(new BattleEvent(
                    eventId(seed, round, events.size()),
                    round,
                    side,
                    "damage",
                    attacker.name()
                            + " dealt "
                            + damage
                            + " damage to "
                            + defender.name()
                            + "."
            ));

            if (defender.defeated()) {
                events.add(new BattleEvent(
                        eventId(
                                seed,
                                round,
                                events.size()
                        ),
                        round,
                        side,
                        "defeat",
                        defender.name()
                                + " was defeated."
                ));
            }
        }
    }

    private int calculateDamage(
            Combatant attacker,
            Combatant defender,
            int round,
            int attackerIndex,
            long seed
    ) {
        long variationSeed =
                seed
                        + (round * 31L)
                        + (attackerIndex * 17L)
                        + attacker.id().getLeastSignificantBits();

        int variation =
                Math.floorMod(variationSeed, 7) - 3;

        int attack =
                attacker.strength()
                        + attacker.level() * 2;

        int defence =
                defender.level()
                        + defender.agility() / 10;

        return Math.max(
                1,
                attack - defence + variation
        );
    }

    private String determineOutcome(
            List<Combatant> players,
            List<Combatant> enemies
    ) {
        if (allDefeated(enemies)) {
            return "victory";
        }

        if (allDefeated(players)) {
            return "defeat";
        }

        int playerHp = remainingHp(players);
        int enemyHp = remainingHp(enemies);

        return playerHp >= enemyHp
                ? "victory"
                : "defeat";
    }

    private int remainingHp(
            List<Combatant> combatants
    ) {
        return combatants
                .stream()
                .mapToInt(Combatant::remainingHp)
                .sum();
    }

    private boolean allDefeated(
            List<Combatant> combatants
    ) {
        return combatants
                .stream()
                .allMatch(Combatant::defeated);
    }

    private Combatant firstActive(
            List<Combatant> combatants
    ) {
        return combatants
                .stream()
                .filter(combatant ->
                        !combatant.defeated()
                )
                .findFirst()
                .orElse(null);
    }

    private Combatant toPlayerCombatant(
            BattleRequest.MonsterInput monster
    ) {
        BattleRequest.StatsInput stats =
                monster.stats();

        return new Combatant(
                monster.id(),
                monster.name(),
                monster.level(),
                stats.strength(),
                stats.hp(),
                stats.accuracy(),
                stats.agility()
        );
    }

    private Combatant toEnemyCombatant(
            BattleRequest.OpponentInput opponent
    ) {
        int level = opponent.level();

        return new Combatant(
                opponent.id(),
                opponent.name(),
                level,
                8 + level * 3,
                60 + level * 20,
                Math.min(100, 55 + level * 3),
                25 + level * 4
        );
    }

    private long battleSeed(
            BattleRequest request
    ) {
        StringBuilder value = new StringBuilder(
                request.encounter()
                        .id()
                        .toString()
        );

        for (
                BattleRequest.MonsterInput monster
                : request.team()
        ) {
            value.append(':')
                    .append(monster.id());
        }

        return value
                .toString()
                .hashCode();
    }

    private String eventId(
            long seed,
            int round,
            int index
    ) {
        return deterministicUuid(
                "event:"
                        + seed
                        + ":"
                        + round
                        + ":"
                        + index
        ).toString();
    }

    private UUID deterministicUuid(
            String value
    ) {
        return UUID.nameUUIDFromBytes(
                value.getBytes(
                        StandardCharsets.UTF_8
                )
        );
    }

    private Instant deterministicPlayedAt(
            long seed
    ) {
        long seconds =
                Math.floorMod(
                        seed,
                        31_536_000L
                );

        return Instant.parse(
                "2026-01-01T00:00:00Z"
        ).plusSeconds(seconds);
    }

    private static final class Combatant {

        private final UUID id;
        private final String name;
        private final int level;
        private final int strength;
        private final int accuracy;
        private final int agility;

        private int remainingHp;

        private Combatant(
                UUID id,
                String name,
                int level,
                int strength,
                int hp,
                int accuracy,
                int agility
        ) {
            this.id = id;
            this.name = name;
            this.level = level;
            this.strength = strength;
            this.remainingHp = hp;
            this.accuracy = accuracy;
            this.agility = agility;
        }

        private UUID id() {
            return id;
        }

        private String name() {
            return name;
        }

        private int level() {
            return level;
        }

        private int strength() {
            return strength;
        }

        private int accuracy() {
            return accuracy;
        }

        private int agility() {
            return agility;
        }

        private int remainingHp() {
            return remainingHp;
        }

        private boolean defeated() {
            return remainingHp <= 0;
        }

        private void takeDamage(
                int damage
        ) {
            remainingHp =
                    Math.max(
                            0,
                            remainingHp - damage
                    );
        }
    }
}