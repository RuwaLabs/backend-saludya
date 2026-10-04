package com.ruwalabs.saludya.arrivalcheckin.domain.model.aggregates;

import com.ruwalabs.saludya.arrivalcheckin.domain.model.valueobjects.AttendanceQueueStatus;
import com.ruwalabs.saludya.shared.domain.model.aggregates.AbstractDomainAggregateRoot;
import lombok.Getter;

import java.time.LocalDate;
import java.util.Objects;

/**
 * AttendanceQueue aggregate root.
 *
 * <p>Represents the attendance queue of a time slot on a given date. It groups the
 * queue entries and is ephemeral (per day and time slot).</p>
 */
@Getter
public class AttendanceQueue extends AbstractDomainAggregateRoot<AttendanceQueue> {

    private final Long id;
    private final Long idTimeSlot;
    private final LocalDate date;
    private AttendanceQueueStatus status;

    public AttendanceQueue(Long id, Long idTimeSlot, LocalDate date, AttendanceQueueStatus status) {
        this.id = id;
        this.idTimeSlot = Objects.requireNonNull(idTimeSlot, "idTimeSlot must not be null");
        this.date = Objects.requireNonNull(date, "date must not be null");
        this.status = Objects.requireNonNull(status, "status must not be null");
    }

    /**
     * Factory method that opens a new attendance queue.
     *
     * @param idTimeSlot the time slot identifier
     * @param date       the queue date
     * @return a new open attendance queue
     */
    public static AttendanceQueue open(Long idTimeSlot, LocalDate date) {
        return new AttendanceQueue(null, idTimeSlot, date, AttendanceQueueStatus.OPEN);
    }

    public void open() {
        this.status = AttendanceQueueStatus.OPEN;
    }

    public void close() {
        this.status = AttendanceQueueStatus.CLOSED;
    }

    public void pause() {
        this.status = AttendanceQueueStatus.PAUSED;
    }

    public boolean isOpen() {
        return this.status == AttendanceQueueStatus.OPEN;
    }
}
