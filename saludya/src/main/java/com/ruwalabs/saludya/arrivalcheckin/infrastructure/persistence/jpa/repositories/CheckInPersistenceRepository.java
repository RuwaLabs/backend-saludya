package com.ruwalabs.saludya.arrivalcheckin.infrastructure.persistence.jpa.repositories;

import com.ruwalabs.saludya.arrivalcheckin.infrastructure.persistence.jpa.entities.CheckInPersistenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Spring Data repository for check-in persistence entities.
 */
@Repository
public interface CheckInPersistenceRepository extends JpaRepository<CheckInPersistenceEntity, Long> {

    Optional<CheckInPersistenceEntity> findByIdAppointment(Long idAppointment);

    boolean existsByIdAppointment(Long idAppointment);
}
