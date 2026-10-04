package com.ruwalabs.saludya.arrivalcheckin.application.queryservices;

import com.ruwalabs.saludya.arrivalcheckin.application.queries.GetCheckInByAppointmentQuery;
import com.ruwalabs.saludya.arrivalcheckin.application.queries.GetCheckInByIdQuery;
import com.ruwalabs.saludya.arrivalcheckin.domain.model.aggregates.CheckIn;

import java.util.Optional;

/**
 * Query service for check-ins.
 */
public interface CheckInQueryService {

    Optional<CheckIn> handle(GetCheckInByIdQuery query);

    Optional<CheckIn> handle(GetCheckInByAppointmentQuery query);
}
