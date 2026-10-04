package com.ruwalabs.saludya.iam.infrastructure.persistence.jpa.repositories;

import com.ruwalabs.saludya.iam.infrastructure.persistence.jpa.entities.PatientEntity;
import org.springframework.data.jpa.repository.*;
import java.util.*;
public interface PatientJpaRepository extends JpaRepository<PatientEntity,Long> {
    Optional<PatientEntity> findByUserId(Long userId);
    Optional<PatientEntity> findByDni(String dni);
    @Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from PatientEntity p where p.id = :id") Optional<PatientEntity> lockById(Long id);
}
