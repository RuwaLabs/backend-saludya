package com.ruwalabs.saludya.hospitaloperations.hospitalconfiguration.domain.model.aggregates;

import com.ruwalabs.saludya.hospitaloperations.hospitalconfiguration.domain.model.enums.BookingOrderScope;
import com.ruwalabs.saludya.shared.domain.model.aggregates.AbstractDomainAggregateRoot;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Objects;

/**
 * Aggregate root that represents the operational configuration
 * of the healthcare facility.
 */
public class HospitalConfiguration
        extends AbstractDomainAggregateRoot<HospitalConfiguration> {

    private Long id;

    private int maxCapacityPerSlot;

    private BookingOrderScope bookingOrderScope;

    private int checkInToleranceMinutes;

    private int postCallToleranceMinutes;

    private int reassignmentResponseTimeoutMin;

    private LocalTime bookingCutoffTime;

    private int cancellationDeadlineHours;

    private boolean attendanceQueueVisible;

    private LocalDateTime updatedAt;

    private HospitalConfiguration(
            Long id,
            int maxCapacityPerSlot,
            BookingOrderScope bookingOrderScope,
            int checkInToleranceMinutes,
            int postCallToleranceMinutes,
            int reassignmentResponseTimeoutMin,
            LocalTime bookingCutoffTime,
            int cancellationDeadlineHours,
            boolean attendanceQueueVisible,
            LocalDateTime updatedAt
    ) {
        this.id = id;
        this.maxCapacityPerSlot = maxCapacityPerSlot;
        this.bookingOrderScope = bookingOrderScope;
        this.checkInToleranceMinutes = checkInToleranceMinutes;
        this.postCallToleranceMinutes = postCallToleranceMinutes;
        this.reassignmentResponseTimeoutMin = reassignmentResponseTimeoutMin;
        this.bookingCutoffTime = bookingCutoffTime;
        this.cancellationDeadlineHours = cancellationDeadlineHours;
        this.attendanceQueueVisible = attendanceQueueVisible;
        this.updatedAt = updatedAt;

        validate();
    }

    /**
     * Creates a new hospital configuration.
     */
    public static HospitalConfiguration create(
            int maxCapacityPerSlot,
            BookingOrderScope bookingOrderScope,
            int checkInToleranceMinutes,
            int postCallToleranceMinutes,
            int reassignmentResponseTimeoutMin,
            LocalTime bookingCutoffTime,
            int cancellationDeadlineHours,
            boolean attendanceQueueVisible
    ) {
        return new HospitalConfiguration(
                null,
                maxCapacityPerSlot,
                bookingOrderScope,
                checkInToleranceMinutes,
                postCallToleranceMinutes,
                reassignmentResponseTimeoutMin,
                bookingCutoffTime,
                cancellationDeadlineHours,
                attendanceQueueVisible,
                LocalDateTime.now()
        );
    }

    /**
     * Reconstitutes an existing aggregate from persistence.
     */
    public static HospitalConfiguration reconstitute(
            Long id,
            int maxCapacityPerSlot,
            BookingOrderScope bookingOrderScope,
            int checkInToleranceMinutes,
            int postCallToleranceMinutes,
            int reassignmentResponseTimeoutMin,
            LocalTime bookingCutoffTime,
            int cancellationDeadlineHours,
            boolean attendanceQueueVisible,
            LocalDateTime updatedAt
    ) {
        return new HospitalConfiguration(
                id,
                maxCapacityPerSlot,
                bookingOrderScope,
                checkInToleranceMinutes,
                postCallToleranceMinutes,
                reassignmentResponseTimeoutMin,
                bookingCutoffTime,
                cancellationDeadlineHours,
                attendanceQueueVisible,
                updatedAt
        );
    }

    public void updateMaxCapacity(int value) {
        validatePositive(value, "Maximum capacity per slot");

        this.maxCapacityPerSlot = value;
        touch();
    }

    public void updateBookingOrderScope(BookingOrderScope value) {
        this.bookingOrderScope = Objects.requireNonNull(
                value,
                "Booking order scope cannot be null"
        );

        touch();
    }

    public void updateTolerances(
            int checkInToleranceMinutes,
            int postCallToleranceMinutes
    ) {
        validateNonNegative(
                checkInToleranceMinutes,
                "Check-in tolerance"
        );

        validateNonNegative(
                postCallToleranceMinutes,
                "Post-call tolerance"
        );

        this.checkInToleranceMinutes = checkInToleranceMinutes;
        this.postCallToleranceMinutes = postCallToleranceMinutes;

        touch();
    }

    public void updateReassignmentTimeout(int value) {
        validateNonNegative(
                value,
                "Reassignment response timeout"
        );

        this.reassignmentResponseTimeoutMin = value;
        touch();
    }

    public void updateBookingCutoffTime(LocalTime value) {
        this.bookingCutoffTime = Objects.requireNonNull(
                value,
                "Booking cutoff time cannot be null"
        );

        touch();
    }

    public void updateCancellationDeadline(int value) {
        validateNonNegative(
                value,
                "Cancellation deadline"
        );

        this.cancellationDeadlineHours = value;
        touch();
    }

    public void updateAttendanceQueueVisibility(boolean value) {
        this.attendanceQueueVisible = value;
        touch();
    }

    /**
     * Validates the complete aggregate state.
     */
    public void validate() {
        validatePositive(
                maxCapacityPerSlot,
                "Maximum capacity per slot"
        );

        Objects.requireNonNull(
                bookingOrderScope,
                "Booking order scope cannot be null"
        );

        validateNonNegative(
                checkInToleranceMinutes,
                "Check-in tolerance"
        );

        validateNonNegative(
                postCallToleranceMinutes,
                "Post-call tolerance"
        );

        validateNonNegative(
                reassignmentResponseTimeoutMin,
                "Reassignment response timeout"
        );

        Objects.requireNonNull(
                bookingCutoffTime,
                "Booking cutoff time cannot be null"
        );

        validateNonNegative(
                cancellationDeadlineHours,
                "Cancellation deadline"
        );

        Objects.requireNonNull(
                updatedAt,
                "Updated timestamp cannot be null"
        );
    }

    private void touch() {
        this.updatedAt = LocalDateTime.now();
    }

    private static void validatePositive(int value, String fieldName) {
        if (value <= 0) {
            throw new IllegalArgumentException(
                    fieldName + " must be greater than zero"
            );
        }
    }

    private static void validateNonNegative(int value, String fieldName) {
        if (value < 0) {
            throw new IllegalArgumentException(
                    fieldName + " cannot be negative"
            );
        }
    }

    public Long getId() {
        return id;
    }

    public int getMaxCapacityPerSlot() {
        return maxCapacityPerSlot;
    }

    public BookingOrderScope getBookingOrderScope() {
        return bookingOrderScope;
    }

    public int getCheckInToleranceMinutes() {
        return checkInToleranceMinutes;
    }

    public int getPostCallToleranceMinutes() {
        return postCallToleranceMinutes;
    }

    public int getReassignmentResponseTimeoutMin() {
        return reassignmentResponseTimeoutMin;
    }

    public LocalTime getBookingCutoffTime() {
        return bookingCutoffTime;
    }

    public int getCancellationDeadlineHours() {
        return cancellationDeadlineHours;
    }

    public boolean isAttendanceQueueVisible() {
        return attendanceQueueVisible;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}