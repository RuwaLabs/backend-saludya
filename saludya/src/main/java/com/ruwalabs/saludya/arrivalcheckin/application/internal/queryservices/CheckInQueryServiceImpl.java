package com.ruwalabs.saludya.arrivalcheckin.application.internal.queryservices;

import com.ruwalabs.saludya.arrivalcheckin.application.queries.GetCheckInByAppointmentQuery;
import com.ruwalabs.saludya.arrivalcheckin.application.queries.GetCheckInByIdQuery;
import com.ruwalabs.saludya.arrivalcheckin.application.queryservices.CheckInQueryService;
import com.ruwalabs.saludya.arrivalcheckin.domain.model.aggregates.CheckIn;
import com.ruwalabs.saludya.arrivalcheckin.domain.repositories.CheckInRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Application service that handles check-in queries.
 */
@Service
public class CheckInQueryServiceImpl implements CheckInQueryService {

    private final CheckInRepository checkInRepository;

    public CheckInQueryServiceImpl(CheckInRepository checkInRepository) {
        this.checkInRepository = checkInRepository;
    }

    @Override
    public Optional<CheckIn> handle(GetCheckInByIdQuery query) {
        return checkInRepository.findById(query.id());
    }

    @Override
    public Optional<CheckIn> handle(GetCheckInByAppointmentQuery query) {
        return checkInRepository.findByAppointmentId(query.appointmentId());
    }
}
