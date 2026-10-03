package com.ruwalabs.saludya.appointments.domain.model.repositories;

import com.ruwalabs.saludya.appointments.domain.model.aggregates.Doctor;

import java.util.List;
import java.util.Optional;

/**
 * Read contract for the doctor catalog.
 */
public interface DoctorRepository {

    Doctor save(Doctor doctor);

    Optional<Doctor> findById(Long id);

    List<Doctor> findBySpecialtyId(Long specialtyId);

    List<Doctor> findAll();

    boolean existsById(Long id);
}
