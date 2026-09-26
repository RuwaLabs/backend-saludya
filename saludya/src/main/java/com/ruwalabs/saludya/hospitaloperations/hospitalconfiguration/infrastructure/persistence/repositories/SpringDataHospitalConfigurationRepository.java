package com.ruwalabs.saludya.hospitaloperations.hospitalconfiguration.infrastructure.persistence.repositories;

import com.ruwalabs.saludya.hospitaloperations.hospitalconfiguration.infrastructure.persistence.entities.HospitalConfigurationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Spring Data repository for hospital configuration persistence.
 */
public interface SpringDataHospitalConfigurationRepository
        extends JpaRepository<HospitalConfigurationEntity, Long> {

    Optional<HospitalConfigurationEntity> findFirstByOrderByIdAsc();
}