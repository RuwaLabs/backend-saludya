package com.ruwalabs.saludya.appointments.application.internal.queryservices;

import com.ruwalabs.saludya.appointments.application.queryservices.TimeSlotQueryService;
import com.ruwalabs.saludya.appointments.application.queries.GetAvailableTimeSlotsBySpecialtyQuery;
import com.ruwalabs.saludya.appointments.application.queries.GetTimeSlotByIdQuery;
import com.ruwalabs.saludya.appointments.application.queries.GetTimeSlotsByDoctorAndDateQuery;
import com.ruwalabs.saludya.appointments.domain.model.aggregates.TimeSlot;
import com.ruwalabs.saludya.appointments.domain.model.repositories.TimeSlotRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Implementation of {@link TimeSlotQueryService}.
 */
@Service
@Transactional(readOnly = true)
public class TimeSlotQueryServiceImpl implements TimeSlotQueryService {

    private final TimeSlotRepository repository;

    public TimeSlotQueryServiceImpl(TimeSlotRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<TimeSlot> getById(GetTimeSlotByIdQuery query) {
        return repository.findById(query.id());
    }

    @Override
    public List<TimeSlot> getAvailableBySpecialty(GetAvailableTimeSlotsBySpecialtyQuery query) {
        return repository.findAvailableBySpecialty(query.specialtyId(), query.date());
    }

    @Override
    public List<TimeSlot> getByDoctorAndDate(GetTimeSlotsByDoctorAndDateQuery query) {
        return repository.findByDoctorAndDate(query.doctorId(), query.date());
    }
}
