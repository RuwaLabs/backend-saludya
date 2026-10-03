package com.ruwalabs.saludya.appointments.application.internal.queryservices;

import com.ruwalabs.saludya.appointments.application.queryservices.DoctorQueryService;
import com.ruwalabs.saludya.appointments.application.queries.GetDoctorByIdQuery;
import com.ruwalabs.saludya.appointments.application.queries.GetDoctorsBySpecialtyQuery;
import com.ruwalabs.saludya.appointments.domain.model.aggregates.Doctor;
import com.ruwalabs.saludya.appointments.domain.model.repositories.DoctorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Implementation of {@link DoctorQueryService}.
 */
@Service
@Transactional(readOnly = true)
public class DoctorQueryServiceImpl implements DoctorQueryService {

    private final DoctorRepository repository;

    public DoctorQueryServiceImpl(DoctorRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<Doctor> getBySpecialty(GetDoctorsBySpecialtyQuery query) {
        return repository.findBySpecialtyId(query.specialtyId());
    }

    @Override
    public Optional<Doctor> getById(GetDoctorByIdQuery query) {
        return repository.findById(query.id());
    }
}
