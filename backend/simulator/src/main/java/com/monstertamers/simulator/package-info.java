/**
 * Pure deterministic battle engine. No database, no Spring Data, no knowledge of other modules:
 * snapshots + rules version + seed in, result + ordered events out.
 */
@ApplicationModule(allowedDependencies = {})
package com.monstertamers.simulator;

import org.springframework.modulith.ApplicationModule;
