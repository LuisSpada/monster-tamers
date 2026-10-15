/** Profile, win/loss stats, final-boss progress. Owns tables: player_profiles, player_progress. */
@ApplicationModule(allowedDependencies = {"encounter :: api", "shared"})
package com.monstertamers.player;

import org.springframework.modulith.ApplicationModule;
