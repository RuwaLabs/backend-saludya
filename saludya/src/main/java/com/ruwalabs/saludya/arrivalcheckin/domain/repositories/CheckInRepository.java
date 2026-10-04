package com.ruwalabs.saludya.arrivalcheckin.domain.repositories;

import com.ruwalabs.saludya.arrivalcheckin.domain.model.aggregates.CheckIn;

import java.util.Optional;

/**
 * Repository port for {@link CheckIn} aggregates.
 */
public interface CheckInRepository {

    CheckIn save(CheckIn checkIn);

    Optional<CheckIn> findById(Long id);

    Optional<CheckIn> findByAppointmentId(Long appointmentId);

    boolean existsByAppointmentId(Long appointmentId);
}
