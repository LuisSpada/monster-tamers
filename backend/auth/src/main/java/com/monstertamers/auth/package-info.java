/** Accounts, login, JWT and refresh sessions. Owns tables: users, auth_sessions. */
@ApplicationModule(allowedDependencies = {"shared"})
package com.monstertamers.auth;

import org.springframework.modulith.ApplicationModule;
