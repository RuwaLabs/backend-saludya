package com.ruwalabs.saludya.hospitaloperations.hospitalconfiguration.infrastructure.persistence.entities;

import com.ruwalabs.saludya.hospitaloperations.hospitalconfiguration.domain.model.enums.BookingOrderScope;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * JPA persistence representation of HospitalConfiguration.
 */
@Entity
@Table(name = "hospital_configurations")
public class HospitalConfigurationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(
            name = "max_capacity_per_slot",
            nullable = false
    )
    private int maxCapacityPerSlot;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "booking_order_scope",
            nullable = false,
            length = 20
    )
    private BookingOrderScope bookingOrderScope;

    @Column(
            name = "check_in_tolerance_minutes",
            nullable = false
    )
    private int checkInToleranceMinutes;

    @Column(
            name = "post_call_tolerance_minutes",
            nullable = false
    )
    private int postCallToleranceMinutes;

    @Column(
            name = "reassignment_response_timeout_min",
            nullable = false
    )
    private int reassignmentResponseTimeoutMin;

    @Column(
            name = "booking_cutoff_time",
            nullable = false
    )
    private LocalTime bookingCutoffTime;

    @Column(
            name = "cancellation_deadline_hours",
            nullable = false
    )
    private int cancellationDeadlineHours;

    @Column(
            name = "attendance_queue_visible",
            nullable = false
    )
    private boolean attendanceQueueVisible;

    @Column(
            name = "updated_at",
            nullable = false
    )
    private LocalDateTime updatedAt;

    protected HospitalConfigurationEntity() {
        // Required by JPA.
    }

    public HospitalConfigurationEntity(
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
        this.reassignmentResponseTimeoutMin =
                reassignmentResponseTimeoutMin;
        this.bookingCutoffTime = bookingCutoffTime;
        this.cancellationDeadlineHours = cancellationDeadlineHours;
        this.attendanceQueueVisible = attendanceQueueVisible;
        this.updatedAt = updatedAt;
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