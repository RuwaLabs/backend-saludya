package com.ruwalabs.saludya.reassignment.domain.model.valueobjects;

/**
 * Enumeration representing the lifecycle status of a {@code ReassignmentOffer}.
 *
 * <p>A reassignment offer is created in {@code PENDING} state and transitions as follows:</p>
 * <ul>
 *   <li>{@code PENDING} → {@code ACCEPTED}: the candidate accepted the freed slot.</li>
 *   <li>{@code PENDING} → {@code REJECTED}: the candidate declined the freed slot.</li>
 *   <li>{@code PENDING} → {@code EXPIRED}: the candidate did not respond within the window.</li>
 *   <li>{@code ACCEPTED} → {@code ATTENDED}: the accepted candidate physically arrived in time.</li>
 *   <li>{@code ACCEPTED} → {@code ABSENT}: the accepted candidate did not arrive in time.</li>
 * </ul>
 */
public enum ReassignmentStatus {
    /**
     * The offer has been sent and is awaiting the candidate's response.
     */
    PENDING,

    /**
     * The candidate accepted the offer and was reassigned to the freed slot.
     */
    ACCEPTED,

    /**
     * The candidate rejected the offer, keeping their original appointment.
     */
    REJECTED,

    /**
     * The offer expired because the candidate did not respond within the window.
     */
    EXPIRED,

    /**
     * The accepted candidate arrived and was attended.
     */
    ATTENDED,

    /**
     * The accepted candidate did not arrive within the window and lost their slot.
     */
    ABSENT
}
