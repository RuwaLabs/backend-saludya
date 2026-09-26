package com.ruwalabs.saludya.reassignment.interfaces.events;

import java.time.Instant;

/**
 * Integration event that signals a patient was declared absent and their time slot was
 * freed.
 *
 * <p><b>Placeholder.</b> This event will ultimately be owned and published by the
 * {@code Arrival & QR Check-in} bounded context. It lives here temporarily so the
 * {@code Reassignment} context can run independently; once Arrival defines its real
 * integration event, align the package and fields accordingly.</p>
 *
 * @param appointmentId   the identifier of the absent patient's appointment
 * @param freedTimeSlotId the identifier of the time slot that became available
 * @param absentAt        the instant at which the absence was declared
 */
public record PatientAbsentEvent(
        Long appointmentId,
        Long freedTimeSlotId,
        Instant absentAt) {
}
