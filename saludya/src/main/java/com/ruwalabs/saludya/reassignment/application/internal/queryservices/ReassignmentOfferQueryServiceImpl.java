package com.ruwalabs.saludya.reassignment.application.internal.queryservices;

import com.ruwalabs.saludya.reassignment.application.queries.GetPendingByAppointmentQuery;
import com.ruwalabs.saludya.reassignment.application.queries.GetReassignmentOfferByIdQuery;
import com.ruwalabs.saludya.reassignment.application.queryservices.ReassignmentOfferQueryService;
import com.ruwalabs.saludya.reassignment.domain.model.aggregates.ReassignmentOffer;
import com.ruwalabs.saludya.reassignment.domain.repositories.ReassignmentOfferRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReassignmentOfferQueryServiceImpl implements ReassignmentOfferQueryService {

    private final ReassignmentOfferRepository repository;

    public ReassignmentOfferQueryServiceImpl(ReassignmentOfferRepository reassignmentOfferRepository) {
        this.repository = reassignmentOfferRepository;
    }

    @Override
    public List<ReassignmentOffer> handle(GetPendingByAppointmentQuery query) {
        return repository.findPendingByAppointment(query.appointmentId());
    }

    @Override
    public ReassignmentOffer handle(GetReassignmentOfferByIdQuery query) {
        return this.repository.findById(query.id()).orElse(null);
    }
}
