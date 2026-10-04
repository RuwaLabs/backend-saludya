package com.ruwalabs.saludya.appointments.application.internal.outboundservices.acl;

import java.time.LocalTime;

/**
 * ACL port for reading the hospital's operational configuration from the
 * {@code Hospital Operations & Configuration} bounded context.
 */
public interface HospitalConfigurationService {

    /**
     * @return the minimum anticipation (hours) required to cancel an appointment
     */
    int cancellationDeadlineHours();

    /**
     * @return the daily time limit after which new bookings are rejected (nullable = no limit)
     */
    LocalTime bookingCutoffTime();

    /**
     * @return the booking order scope ({@code GLOBAL} or {@code PER_SPECIALTY})
     */
    String bookingOrderScope();
}
