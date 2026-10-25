package com.monstertamers.auth.internal.persistence;

import com.monstertamers.auth.internal.domain.RegistrationAttempt;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface RegistrationAttemptRepository extends JpaRepository<RegistrationAttempt, UUID> {

    @Modifying
    @Query("DELETE FROM RegistrationAttempt a WHERE a.email = :email")
    void deleteAllByEmail(String email);
}
