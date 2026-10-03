package com.ruwalabs.saludya.iam.infrastructure.persistence.jpa.repositories;

import com.ruwalabs.saludya.iam.infrastructure.persistence.jpa.entities.UserAccountEntity;
import org.springframework.data.jpa.repository.*;
import java.util.*;
public interface UserAccountJpaRepository extends JpaRepository<UserAccountEntity,Long> {
    Optional<UserAccountEntity> findByEmail(String email);
    @Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @Query("select u from UserAccountEntity u where u.id = :id") Optional<UserAccountEntity> lockById(Long id);
}
