package com.ruwalabs.saludya.arrivalcheckin.domain.repositories;

import com.ruwalabs.saludya.arrivalcheckin.domain.model.aggregates.AttendanceQueue;

import java.time.LocalDate;
import java.util.Optional;

/**
 * Repository port for {@link AttendanceQueue} aggregates.
 */
public interface AttendanceQueueRepository {

    AttendanceQueue save(AttendanceQueue queue);

    Optional<AttendanceQueue> findById(Long id);

    Optional<AttendanceQueue> findByTimeSlotAndDate(Long timeSlotId, LocalDate date);
}
