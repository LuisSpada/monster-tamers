/** Accounts, login, JWT and refresh sessions. Owns tables: users, auth_sessions. */
@ApplicationModule(allowedDependencies = {"player :: api", "monster :: api", "shared"})
package com.monstertamers.auth;

import org.springframework.modulith.ApplicationModule;
