package com.ruwalabs.saludya.arrivalcheckin.infrastructure.acl;

import com.ruwalabs.saludya.arrivalcheckin.application.internal.outboundservices.acl.HospitalConfigurationService;
import com.ruwalabs.saludya.hospitaloperations.hospitalconfiguration.interfaces.acl.HospitalConfigurationContextFacade;
import org.springframework.stereotype.Service;

/**
 * Real ACL adapter for {@link HospitalConfigurationService} backed by the
 * {@code Hospital Operations & Configuration} facade.
 */
@Service("arrivalHospitalConfigurationService")
public class BookingHospitalConfigurationService implements HospitalConfigurationService {

    private final HospitalConfigurationContextFacade configurationContextFacade;

    public BookingHospitalConfigurationService(HospitalConfigurationContextFacade configurationContextFacade) {
        this.configurationContextFacade = configurationContextFacade;
    }

    @Override
    public int checkInToleranceMinutes() {
        return configurationContextFacade.checkInToleranceMinutes();
    }

    @Override
    public int postCallToleranceMinutes() {
        return configurationContextFacade.postCallToleranceMinutes();
    }

    @Override
    public boolean attendanceQueueVisible() {
        return configurationContextFacade.attendanceQueueVisible();
    }
}
