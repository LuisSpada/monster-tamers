/**
 * Technical building blocks without game logic: errors, time, randomness, current-user access.
 * Open module: every other module may use any of its packages.
 */
@ApplicationModule(type = ApplicationModule.Type.OPEN)
package com.monstertamers.shared;

import org.springframework.modulith.ApplicationModule;
