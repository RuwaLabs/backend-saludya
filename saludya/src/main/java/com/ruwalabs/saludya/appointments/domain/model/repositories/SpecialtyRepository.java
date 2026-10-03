package com.ruwalabs.saludya.appointments.domain.model.repositories;

import com.ruwalabs.saludya.appointments.domain.model.aggregates.Specialty;

import java.util.List;
import java.util.Optional;

/**
 * Read contract for the specialty catalog.
 */
public interface SpecialtyRepository {

    Specialty save(Specialty specialty);

    Optional<Specialty> findById(Long id);

    List<Specialty> findAll();

    boolean existsById(Long id);
}
