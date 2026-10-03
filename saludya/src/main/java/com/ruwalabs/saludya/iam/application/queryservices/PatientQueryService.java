package com.ruwalabs.saludya.iam.application.queryservices;

import com.ruwalabs.saludya.iam.domain.model.aggregates.Patient;
import com.ruwalabs.saludya.iam.domain.model.entities.PatientMinor;
import java.util.List;
public interface PatientQueryService {
    Patient getById(Long actorId, Long patientId);
    List<PatientMinor> getMinorsByTutor(Long actorId, Long tutorId);
    PatientMinor getLinkById(Long actorId, Long id);
}
