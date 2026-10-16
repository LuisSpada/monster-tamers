/** Profile, win/loss stats, final-boss progress. Owns tables: player_profiles, player_progress. */
@ApplicationModule(allowedDependencies = {"auth :: api", "shared"})
package com.monstertamers.player;

import org.springframework.modulith.ApplicationModule;
