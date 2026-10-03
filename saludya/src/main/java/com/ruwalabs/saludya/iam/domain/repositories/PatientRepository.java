package com.ruwalabs.saludya.iam.domain.repositories;

import com.ruwalabs.saludya.iam.domain.model.aggregates.Patient;
import java.util.Optional;
public interface PatientRepository {
    Patient save(Patient patient);
    Optional<Patient> findById(Long id);
    Optional<Patient> findByDni(String dni);
    Optional<Patient> findByUserId(Long id);
    Optional<Patient> lockById(Long id);
}
