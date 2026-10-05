package com.ruwalabs.saludya.arrivalcheckin.application.queryservices;

import com.ruwalabs.saludya.arrivalcheckin.application.queries.GetAttendanceQueueBySlotAndDateQuery;
import com.ruwalabs.saludya.arrivalcheckin.application.queries.GetQueueEntriesQuery;
import com.ruwalabs.saludya.arrivalcheckin.application.queries.GetQueueEntryByIdQuery;
import com.ruwalabs.saludya.arrivalcheckin.application.queries.GetQueuePositionQuery;
import com.ruwalabs.saludya.arrivalcheckin.domain.model.aggregates.AttendanceQueue;
import com.ruwalabs.saludya.arrivalcheckin.domain.model.entities.QueueEntry;
import com.ruwalabs.saludya.arrivalcheckin.domain.model.valueobjects.QueuePosition;

import java.util.List;
import java.util.Optional;

/**
 * Query service for the attendance queue.
 */
public interface QueueQueryService {

    Optional<QueueEntry> handle(GetQueueEntryByIdQuery query);

    List<QueueEntry> handle(GetQueueEntriesQuery query);

    Optional<QueuePosition> handle(GetQueuePositionQuery query);

    /**
     * Resolves the attendance queue of a time slot on a given date.
     *
     * @param query the resolution query
     * @return the queue, if it has been opened
     */
    Optional<AttendanceQueue> handle(GetAttendanceQueueBySlotAndDateQuery query);

    /**
     * Gets the queue position of the patient of a given appointment.
     *
     * @param appointmentId the appointment identifier
     * @return the position, if the patient has a queue entry
     */
    Optional<QueuePosition> getPositionByAppointment(Long appointmentId);
}
