package com.ruwalabs.saludya.appointments.application.queryservices;

import com.ruwalabs.saludya.appointments.application.queries.GetAppointmentByIdQuery;
import com.ruwalabs.saludya.appointments.application.queries.GetAppointmentsByPatientQuery;
import com.ruwalabs.saludya.appointments.application.queries.GetAppointmentsByTimeSlotQuery;
import com.ruwalabs.saludya.appointments.domain.model.aggregates.Appointment;

import java.util.List;
import java.util.Optional;

/**
 * Query service for appointment reads.
 */
public interface AppointmentQueryService {

    Optional<Appointment> getById(GetAppointmentByIdQuery query);

    List<Appointment> getByPatient(GetAppointmentsByPatientQuery query);

    List<Appointment> getByTimeSlot(GetAppointmentsByTimeSlotQuery query);
}
