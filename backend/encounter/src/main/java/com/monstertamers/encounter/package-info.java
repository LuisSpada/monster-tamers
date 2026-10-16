/**
 * Encounter list and unlocks, battle orchestration, rewards and history.
 * Owns tables: encounter_definitions, player_encounter_progress, encounter_runs, participants, events, rewards.
 */
@ApplicationModule(allowedDependencies = {"monster :: api", "player :: api", "simulator :: api", "shared"})
package com.monstertamers.encounter;

import org.springframework.modulith.ApplicationModule;
