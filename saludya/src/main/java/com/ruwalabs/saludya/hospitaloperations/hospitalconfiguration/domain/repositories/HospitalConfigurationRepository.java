package com.ruwalabs.saludya.hospitaloperations.hospitalconfiguration.domain.repositories;

import com.ruwalabs.saludya.hospitaloperations.hospitalconfiguration.domain.model.aggregates.HospitalConfiguration;

import java.util.Optional;

/**
 * Repository port for the HospitalConfiguration aggregate.
 */
public interface HospitalConfigurationRepository {

    HospitalConfiguration save(HospitalConfiguration configuration);

    Optional<HospitalConfiguration> findDefault();

    HospitalConfiguration update(HospitalConfiguration configuration);
}