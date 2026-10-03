package com.ruwalabs.saludya.appointments.application.queryservices;

import com.ruwalabs.saludya.appointments.application.queries.GetAllSpecialtiesQuery;
import com.ruwalabs.saludya.appointments.application.queries.GetSpecialtyByIdQuery;
import com.ruwalabs.saludya.appointments.domain.model.aggregates.Specialty;

import java.util.List;
import java.util.Optional;

/**
 * Query service for specialty catalog reads.
 */
public interface SpecialtyQueryService {

    List<Specialty> getAll(GetAllSpecialtiesQuery query);

    Optional<Specialty> getById(GetSpecialtyByIdQuery query);
}
