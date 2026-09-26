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
}
