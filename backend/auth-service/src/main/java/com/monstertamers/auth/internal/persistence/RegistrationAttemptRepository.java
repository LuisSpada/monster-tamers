package com.monstertamers.auth.internal.persistence;

import com.monstertamers.auth.internal.domain.RegistrationAttempt;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Query;

public interface RegistrationAttemptRepository extends JpaRepository<RegistrationAttempt, UUID> {

    @Modifying
    @Query("DELETE FROM RegistrationAttempt a WHERE a.email = :email")
    void deleteAllByEmail(String email);

    @Modifying
    @Query("""
            UPDATE RegistrationAttempt a
            SET a.failedAttempts = a.failedAttempts + 1
            WHERE a.id = :id AND a.failedAttempts < :max
            """)
    int consumeAttempt(@Param("id") UUID id, @Param("max") int max);
}
