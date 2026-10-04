package com.ruwalabs.saludya.reassignment.application.internal.queryservices;

import com.ruwalabs.saludya.reassignment.application.queries.GetOfferByIdQuery;
import com.ruwalabs.saludya.reassignment.application.queries.GetPendingOffersByAppointmentQuery;
import com.ruwalabs.saludya.reassignment.application.queryservices.ReassignmentQueryService;
import com.ruwalabs.saludya.reassignment.application.internal.outboundservices.acl.AppointmentLookupService;
import com.ruwalabs.saludya.reassignment.application.internal.outboundservices.acl.PatientAccessService;
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
    private final AppointmentLookupService appointmentLookupService;
    private final PatientAccessService patientAccessService;

    public ReassignmentQueryServiceImpl(
            ReassignmentOfferRepository reassignmentOfferRepository,
            AppointmentLookupService appointmentLookupService,
            PatientAccessService patientAccessService) {
        this.reassignmentOfferRepository = reassignmentOfferRepository;
        this.appointmentLookupService = appointmentLookupService;
        this.patientAccessService = patientAccessService;
    }

    @Override
    public Optional<ReassignmentOffer> handle(GetOfferByIdQuery query) {
        return reassignmentOfferRepository.findById(query.offerId());
    }

    @Override
    public List<ReassignmentOffer> handle(GetPendingOffersByAppointmentQuery query) {
        return reassignmentOfferRepository.findPendingByAppointment(query.appointmentId());
    }

    @Override
    public List<ReassignmentOffer> getPendingForCurrentUser() {
        return reassignmentOfferRepository.findAllPending().stream()
                .filter(offer -> appointmentLookupService.patientOfAppointment(offer.getAppointmentId())
                        .map(patientAccessService::canManagePatient)
                        .orElse(false))
                .toList();
    }
}
