package com.ruwalabs.saludya.appointments.application.internal.queryservices;

import com.ruwalabs.saludya.appointments.application.queryservices.AppointmentQueryService;
import com.ruwalabs.saludya.appointments.application.queries.GetAppointmentByIdQuery;
import com.ruwalabs.saludya.appointments.application.queries.GetAppointmentsByPatientQuery;
import com.ruwalabs.saludya.appointments.application.queries.GetAppointmentsByTimeSlotQuery;
import com.ruwalabs.saludya.appointments.domain.model.aggregates.Appointment;
import com.ruwalabs.saludya.appointments.domain.model.repositories.AppointmentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Implementation of {@link AppointmentQueryService}.
 */
@Service
@Transactional(readOnly = true)
public class AppointmentQueryServiceImpl implements AppointmentQueryService {

    private final AppointmentRepository repository;

    public AppointmentQueryServiceImpl(AppointmentRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<Appointment> getById(GetAppointmentByIdQuery query) {
        return repository.findById(query.id());
    }

    @Override
    public List<Appointment> getByPatient(GetAppointmentsByPatientQuery query) {
        return repository.findByPatientId(query.patientId());
    }

    @Override
    public List<Appointment> getByTimeSlot(GetAppointmentsByTimeSlotQuery query) {
        return repository.findByTimeSlotId(query.timeSlotId());
    }
}
