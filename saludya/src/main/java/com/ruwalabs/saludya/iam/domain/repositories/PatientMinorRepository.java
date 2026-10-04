package com.ruwalabs.saludya.iam.domain.repositories;

import com.ruwalabs.saludya.iam.domain.model.entities.PatientMinor;
import java.util.*;
public interface PatientMinorRepository {
    PatientMinor save(PatientMinor minor);
    Optional<PatientMinor> findById(Long id);
    Optional<PatientMinor> findByPatientId(Long id);
    List<PatientMinor> findMinorsByTutor(Long tutorId);
    void delete(PatientMinor minor);
}
