package com.ruwalabs.saludya.reassignment.application.internal.queryservices;

import com.ruwalabs.saludya.reassignment.application.queries.GetOfferByIdQuery;
import com.ruwalabs.saludya.reassignment.application.queries.GetPendingOffersByAppointmentQuery;
import com.ruwalabs.saludya.reassignment.application.queryservices.ReassignmentQueryService;
import com.ruwalabs.saludya.reassignment.domain.model.aggregates.ReassignmentOffer;
import com.ruwalabs.saludya.reassignment.domain.repositories.ReassignmentOfferRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Application service that resolves reassignment read queries.
 */
@Service
public class ReassignmentQueryServiceImpl implements ReassignmentQueryService {

    private final ReassignmentOfferRepository reassignmentOfferRepository;

    public ReassignmentQueryServiceImpl(ReassignmentOfferRepository reassignmentOfferRepository) {
        this.reassignmentOfferRepository = reassignmentOfferRepository;
    }

    @Override
    public Optional<ReassignmentOffer> handle(GetOfferByIdQuery query) {
        return reassignmentOfferRepository.findById(query.offerId());
    }

    @Override
    public List<ReassignmentOffer> handle(GetPendingOffersByAppointmentQuery query) {
        return reassignmentOfferRepository.findPendingByAppointment(query.appointmentId());
    }
}
