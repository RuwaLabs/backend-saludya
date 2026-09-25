package com.ruwalabs.saludya.reassignment.application.queryservices;

import com.ruwalabs.saludya.reassignment.application.queries.GetPendingByAppointmentQuery;
import com.ruwalabs.saludya.reassignment.application.queries.GetReassignmentOfferByIdQuery;
import com.ruwalabs.saludya.reassignment.domain.model.aggregates.ReassignmentOffer;

import java.util.List;

public interface ReassignmentOfferQueryService {

    List<ReassignmentOffer> handle(GetPendingByAppointmentQuery query);
    ReassignmentOffer handle(GetReassignmentOfferByIdQuery query);
}
