package com.ruwalabs.saludya.reassignment.application.internal.outboundservices.acl;

/**
 * Port for reading the operational parameters that the {@code Reassignment} bounded
 * context needs from the {@code Hospital Operations & Configuration} context.
 */
public interface HospitalConfigurationService {

    /**
     * Returns the single window (in minutes) a candidate has to accept a reassignment
     * offer and physically arrive. The offer's {@code expiresAt} is computed from it.
     *
     * @return the reassignment window, in minutes
     */
    int reassignmentResponseTimeoutMinutes();

    /**
     * Returns the check-in tolerance (in minutes) after a time slot starts. It defines
     * the arrival deadline for an accepted reassignment candidate.
     *
     * @return the check-in tolerance, in minutes
     */
    int checkInToleranceMinutes();
}
