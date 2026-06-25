package com.ecofocus.repository;

import com.ecofocus.entity.PendingRegistration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import java.util.Optional;

public interface PendingRegistrationRepository extends JpaRepository<PendingRegistration, Long> {
    Optional<PendingRegistration> findByEmailAndCode(String email, String code);

    @Modifying
    @Query("DELETE FROM PendingRegistration p WHERE p.email = :email")
    void deleteAllByEmail(String email);
}
