package com.ruwalabs.saludya.reassignment.domain.model.valueobjects;

/**
 * Enumeration representing the lifecycle status of a {@code ReassignmentOffer}.
 *
 * <p>A reassignment offer is created in {@code PENDING} state and can transition
 * to {@code ACCEPTED} (patient accepts the freed slot), {@code REJECTED} (patient
 * declines the freed slot) or {@code EXPIRED} (patient does not respond before the
 * configured {@code reassignmentResponseTimeoutMin}).</p>
 */
public enum ReassignmentStatus {
    /**
     * The offer has been sent and is awaiting the patient's response.
     */
    PENDING,

    /**
     * The patient accepted the offer and the appointment was reassigned to the freed slot.
     */
    ACCEPTED,

    /**
     * The patient rejected the offer, keeping their original appointment.
     */
    REJECTED,

    /**
     * The offer expired because the patient did not respond within the timeout.
     */
    EXPIRED
}
