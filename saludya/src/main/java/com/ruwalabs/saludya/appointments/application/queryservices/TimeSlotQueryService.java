package com.ruwalabs.saludya.appointments.application.queryservices;

import com.ruwalabs.saludya.appointments.application.queries.GetAvailableTimeSlotsBySpecialtyQuery;
import com.ruwalabs.saludya.appointments.application.queries.GetTimeSlotByIdQuery;
import com.ruwalabs.saludya.appointments.application.queries.GetTimeSlotsByDoctorAndDateQuery;
import com.ruwalabs.saludya.appointments.domain.model.aggregates.TimeSlot;

import java.util.List;
import java.util.Optional;

/**
 * Query service for time slot reads.
 */
public interface TimeSlotQueryService {

    Optional<TimeSlot> getById(GetTimeSlotByIdQuery query);

    List<TimeSlot> getAvailableBySpecialty(GetAvailableTimeSlotsBySpecialtyQuery query);

    List<TimeSlot> getByDoctorAndDate(GetTimeSlotsByDoctorAndDateQuery query);
}
