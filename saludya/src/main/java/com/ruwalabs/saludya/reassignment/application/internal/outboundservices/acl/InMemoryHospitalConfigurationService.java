package com.ruwalabs.saludya.reassignment.application.internal.outboundservices.acl;

import org.springframework.stereotype.Service;

/**
 * In-memory stub for {@link HospitalConfigurationService}.
 *
 * <p>TODO: replace with the real implementation that calls the
 * {@code HospitalOperationsContextFacade} once the Hospital Operations &amp;
 * Configuration context is ready.</p>
 */
@Service
public class InMemoryHospitalConfigurationService implements HospitalConfigurationService {

    private static final int DEFAULT_REASSIGNMENT_WINDOW_MINUTES = 10;

    @Override
    public int reassignmentResponseTimeoutMinutes() {
        return DEFAULT_REASSIGNMENT_WINDOW_MINUTES;
    }
}
