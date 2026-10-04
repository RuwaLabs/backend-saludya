package com.ruwalabs.saludya.hospitaloperations.hospitalconfiguration.interfaces.acl;

import com.ruwalabs.saludya.hospitaloperations.hospitalconfiguration.application.services.ConfigurationQueryService;
import com.ruwalabs.saludya.hospitaloperations.hospitalconfiguration.domain.model.aggregates.HospitalConfiguration;
import org.springframework.stereotype.Service;

import java.time.LocalTime;

/**
 * Facade that other bounded contexts use to read the hospital's operational
 * configuration. It is the provider side of the ACL used by
 * {@code Arrival & QR Check-in}, {@code Reassignment} and {@code Appointments & Booking}.
 */
@Service
public class HospitalConfigurationContextFacade {

    private static final int DEFAULT_CHECK_IN_TOLERANCE_MIN = 15;
    private static final int DEFAULT_POST_CALL_TOLERANCE_MIN = 5;
    private static final int DEFAULT_REASSIGNMENT_TIMEOUT_MIN = 10;
    private static final int DEFAULT_CANCELLATION_DEADLINE_HOURS = 24;
    private static final int DEFAULT_MAX_CAPACITY_PER_SLOT = 1;

    private final ConfigurationQueryService queryService;

    public HospitalConfigurationContextFacade(ConfigurationQueryService queryService) {
        this.queryService = queryService;
    }

    public int checkInToleranceMinutes() {
        var config = configuration();
        return config != null ? config.getCheckInToleranceMinutes() : DEFAULT_CHECK_IN_TOLERANCE_MIN;
    }

    public int postCallToleranceMinutes() {
        var config = configuration();
        return config != null ? config.getPostCallToleranceMinutes() : DEFAULT_POST_CALL_TOLERANCE_MIN;
    }

    public int reassignmentResponseTimeoutMin() {
        var config = configuration();
        return config != null ? config.getReassignmentResponseTimeoutMin() : DEFAULT_REASSIGNMENT_TIMEOUT_MIN;
    }

    public int cancellationDeadlineHours() {
        var config = configuration();
        return config != null ? config.getCancellationDeadlineHours() : DEFAULT_CANCELLATION_DEADLINE_HOURS;
    }

    public int maxCapacityPerSlot() {
        var config = configuration();
        return config != null ? config.getMaxCapacityPerSlot() : DEFAULT_MAX_CAPACITY_PER_SLOT;
    }

    public LocalTime bookingCutoffTime() {
        var config = configuration();
        return config != null ? config.getBookingCutoffTime() : null;
    }

    public String bookingOrderScope() {
        var config = configuration();
        return config != null ? config.getBookingOrderScope().name() : "PER_SPECIALTY";
    }

    public boolean attendanceQueueVisible() {
        var config = configuration();
        return config == null || config.isAttendanceQueueVisible();
    }

    private HospitalConfiguration configuration() {
        try {
            return queryService.getConfiguration();
        } catch (Exception ex) {
            return null;
        }
    }
}
