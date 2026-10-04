package com.ruwalabs.saludya.appointments.infrastructure.acl;

import com.ruwalabs.saludya.appointments.application.internal.outboundservices.acl.HospitalConfigurationService;
import com.ruwalabs.saludya.hospitaloperations.hospitalconfiguration.interfaces.acl.HospitalConfigurationContextFacade;
import org.springframework.stereotype.Service;

import java.time.LocalTime;

/**
 * Real ACL adapter for {@link HospitalConfigurationService} backed by the
 * {@code Hospital Operations & Configuration} facade.
 */
@Service("appointmentsHospitalConfigurationService")
public class BookingHospitalConfigurationService implements HospitalConfigurationService {

    private final HospitalConfigurationContextFacade configurationContextFacade;

    public BookingHospitalConfigurationService(HospitalConfigurationContextFacade configurationContextFacade) {
        this.configurationContextFacade = configurationContextFacade;
    }

    @Override
    public int cancellationDeadlineHours() {
        return configurationContextFacade.cancellationDeadlineHours();
    }

    @Override
    public LocalTime bookingCutoffTime() {
        return configurationContextFacade.bookingCutoffTime();
    }

    @Override
    public String bookingOrderScope() {
        return configurationContextFacade.bookingOrderScope();
    }
}
