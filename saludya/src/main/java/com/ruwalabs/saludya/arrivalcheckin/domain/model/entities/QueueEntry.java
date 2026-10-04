package com.ruwalabs.saludya.arrivalcheckin.domain.model.entities;

import com.ruwalabs.saludya.arrivalcheckin.domain.model.valueobjects.QueueEntryStatus;
import com.ruwalabs.saludya.shared.domain.model.aggregates.AbstractDomainAggregateRoot;
import lombok.Getter;

import java.time.Instant;
import java.util.Objects;

/**
 * QueueEntry entity.
 *
 * <p>Represents an individual person in the attendance queue. Its position is
 * determined by the check-in timestamp order.</p>
 */
@Getter
public class QueueEntry extends AbstractDomainAggregateRoot<QueueEntry> {

    private final Long id;
    private final Long idAttendanceQueue;
    private final Long idCheckIn;
    private int position;
    private QueueEntryStatus status;
    private Instant calledAt;
    private Instant attendedAt;

    public QueueEntry(
            Long id,
            Long idAttendanceQueue,
            Long idCheckIn,
            int position,
            QueueEntryStatus status,
            Instant calledAt,
            Instant attendedAt) {
        this.id = id;
        this.idAttendanceQueue = Objects.requireNonNull(idAttendanceQueue, "idAttendanceQueue must not be null");
        this.idCheckIn = Objects.requireNonNull(idCheckIn, "idCheckIn must not be null");
        this.position = position;
        this.status = Objects.requireNonNull(status, "status must not be null");
        this.calledAt = calledAt;
        this.attendedAt = attendedAt;
    }

    /**
     * Factory method that creates a waiting queue entry.
     *
     * @param idAttendanceQueue the attendance queue identifier
     * @param idCheckIn         the check-in identifier
     * @param position          the assigned position
     * @return a new waiting queue entry
     */
    public static QueueEntry create(Long idAttendanceQueue, Long idCheckIn, int position) {
        return new QueueEntry(null, idAttendanceQueue, idCheckIn, position, QueueEntryStatus.WAITING, null, null);
    }

    /**
     * Calls the patient to the consultation room.
     *
     * @throws IllegalStateException if the entry is not waiting
     */
    public void call() {
        if (!isWaiting()) {
            throw new IllegalStateException("Queue entry is not waiting");
        }
        this.status = QueueEntryStatus.CALLED;
        this.calledAt = Instant.now();
    }

    /**
     * Marks the entry as in attention.
     *
     * @throws IllegalStateException if the entry has not been called
     */
    public void startAttention() {
        if (this.status != QueueEntryStatus.CALLED) {
            throw new IllegalStateException("Queue entry has not been called");
        }
        this.status = QueueEntryStatus.IN_ATTENTION;
    }

    /**
     * Marks the entry as attended (the consultation finished).
     *
     * @throws IllegalStateException if the entry is not in attention
     */
    public void markAsAttended() {
        if (this.status != QueueEntryStatus.IN_ATTENTION && this.status != QueueEntryStatus.CALLED) {
            throw new IllegalStateException("Queue entry is not being attended");
        }
        this.status = QueueEntryStatus.ATTENDED;
        this.attendedAt = Instant.now();
    }

    /**
     * Marks the entry as absent.
     *
     * @throws IllegalStateException if the entry was already attended
     */
    public void markAsAbsent() {
        if (this.status == QueueEntryStatus.ATTENDED || this.status == QueueEntryStatus.ABSENT) {
            throw new IllegalStateException("Queue entry is already closed");
        }
        this.status = QueueEntryStatus.ABSENT;
    }

    /**
     * Indicates whether the entry is still waiting.
     *
     * @return {@code true} if waiting
     */
    public boolean isWaiting() {
        return this.status == QueueEntryStatus.WAITING;
    }
}
