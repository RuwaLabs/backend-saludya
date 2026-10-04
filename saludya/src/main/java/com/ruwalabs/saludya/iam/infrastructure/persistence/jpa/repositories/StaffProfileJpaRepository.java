package com.ruwalabs.saludya.iam.infrastructure.persistence.jpa.repositories;

import com.ruwalabs.saludya.iam.infrastructure.persistence.jpa.entities.StaffProfileEntity;
import org.springframework.data.jpa.repository.*;
import java.util.*;
public interface StaffProfileJpaRepository extends JpaRepository<StaffProfileEntity,Long> {
    Optional<StaffProfileEntity> findByUserId(Long userId);
    Optional<StaffProfileEntity> findByDni(String dni);
}
