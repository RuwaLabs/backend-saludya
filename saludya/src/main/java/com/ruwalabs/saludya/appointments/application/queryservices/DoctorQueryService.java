package com.ruwalabs.saludya.appointments.application.queryservices;

import com.ruwalabs.saludya.appointments.application.queries.GetDoctorByIdQuery;
import com.ruwalabs.saludya.appointments.application.queries.GetDoctorsBySpecialtyQuery;
import com.ruwalabs.saludya.appointments.domain.model.aggregates.Doctor;

import java.util.List;
import java.util.Optional;

/**
 * Query service for doctor catalog reads.
 */
public interface DoctorQueryService {

    List<Doctor> getBySpecialty(GetDoctorsBySpecialtyQuery query);

    Optional<Doctor> getById(GetDoctorByIdQuery query);
}
