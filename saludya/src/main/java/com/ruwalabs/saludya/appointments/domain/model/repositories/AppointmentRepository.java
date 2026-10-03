package com.ruwalabs.saludya.appointments.domain.model.repositories;

import com.ruwalabs.saludya.appointments.domain.model.aggregates.Appointment;
import com.ruwalabs.saludya.appointments.domain.model.valueobjects.AppointmentStatus;

import java.util.List;
import java.util.Optional;

/**
 * Persistence contract for appointments.
 */
public interface AppointmentRepository {

    Appointment save(Appointment appointment);

    Optional<Appointment> findById(Long id);

    List<Appointment> findByPatientId(Long patientId);

    List<Appointment> findByTimeSlotId(Long timeSlotId);

    List<Appointment> findActiveBySpecialtyId(Long specialtyId);

    int getNextBookingOrderBySpecialty(Long specialtyId);

    boolean existsActiveByPatientAndTimeSlot(Long patientId, Long timeSlotId);

    void updateStatus(Long id, AppointmentStatus status);

    boolean existsById(Long id);

    void deleteById(Long id);
}
