package com.ruwalabs.saludya.reassignment.infrastructure.acl;

import com.ruwalabs.saludya.hospitaloperations.hospitalconfiguration.interfaces.acl.HospitalConfigurationContextFacade;
import com.ruwalabs.saludya.reassignment.application.internal.outboundservices.acl.HospitalConfigurationService;
import org.springframework.stereotype.Service;

/**
 * Real ACL adapter for {@link HospitalConfigurationService} backed by the
 * {@code Hospital Operations & Configuration} facade.
 */
@Service("reassignmentHospitalConfigurationService")
public class BookingHospitalConfigurationService implements HospitalConfigurationService {

    private final HospitalConfigurationContextFacade configurationContextFacade;

    public BookingHospitalConfigurationService(HospitalConfigurationContextFacade configurationContextFacade) {
        this.configurationContextFacade = configurationContextFacade;
    }

    @Override
    public int reassignmentResponseTimeoutMinutes() {
        return configurationContextFacade.reassignmentResponseTimeoutMin();
    }

    @Override
    public int checkInToleranceMinutes() {
        return configurationContextFacade.checkInToleranceMinutes();
    }
}
