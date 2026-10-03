package com.ruwalabs.saludya.appointments.domain.model.repositories;

import com.ruwalabs.saludya.appointments.domain.model.aggregates.TimeSlot;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Persistence contract for time slots.
 */
public interface TimeSlotRepository {

    TimeSlot save(TimeSlot timeSlot);

    Optional<TimeSlot> findById(Long id);

    List<TimeSlot> findAvailableBySpecialty(Long specialtyId, LocalDate date);

    List<TimeSlot> findByDoctorAndDate(Long doctorId, LocalDate date);

    boolean existsById(Long id);
}
