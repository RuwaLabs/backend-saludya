package com.ruwalabs.saludya.iam.infrastructure.persistence.jpa.repositories;

import com.ruwalabs.saludya.iam.infrastructure.persistence.jpa.entities.PatientMinorEntity;
import org.springframework.data.jpa.repository.*;
import java.util.*;
public interface PatientMinorJpaRepository extends JpaRepository<PatientMinorEntity,Long> {
    Optional<PatientMinorEntity> findByPatientId(Long patientId);
    List<PatientMinorEntity> findByTutorIdOrderById(Long tutorId);
}
