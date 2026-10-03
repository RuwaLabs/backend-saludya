package com.ruwalabs.saludya.appointments.application.internal.queryservices;

import com.ruwalabs.saludya.appointments.application.queryservices.SpecialtyQueryService;
import com.ruwalabs.saludya.appointments.application.queries.GetAllSpecialtiesQuery;
import com.ruwalabs.saludya.appointments.application.queries.GetSpecialtyByIdQuery;
import com.ruwalabs.saludya.appointments.domain.model.aggregates.Specialty;
import com.ruwalabs.saludya.appointments.domain.model.repositories.SpecialtyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Implementation of {@link SpecialtyQueryService}.
 */
@Service
@Transactional(readOnly = true)
public class SpecialtyQueryServiceImpl implements SpecialtyQueryService {

    private final SpecialtyRepository repository;

    public SpecialtyQueryServiceImpl(SpecialtyRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<Specialty> getAll(GetAllSpecialtiesQuery query) {
        return repository.findAll();
    }

    @Override
    public Optional<Specialty> getById(GetSpecialtyByIdQuery query) {
        return repository.findById(query.id());
    }
}
