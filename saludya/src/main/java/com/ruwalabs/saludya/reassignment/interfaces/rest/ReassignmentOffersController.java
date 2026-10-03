package com.ruwalabs.saludya.reassignment.interfaces.rest;

import com.ruwalabs.saludya.reassignment.application.commands.AcceptReassignmentCommand;
import com.ruwalabs.saludya.reassignment.application.commands.RejectReassignmentCommand;
import com.ruwalabs.saludya.reassignment.application.commandservices.ReassignmentCommandService;
import com.ruwalabs.saludya.reassignment.application.queries.GetPendingOffersByAppointmentQuery;
import com.ruwalabs.saludya.reassignment.application.queryservices.ReassignmentQueryService;
import com.ruwalabs.saludya.reassignment.interfaces.rest.resources.ReassignmentOfferResource;
import com.ruwalabs.saludya.reassignment.interfaces.rest.transform.ReassignmentOfferResourceAssembler;
import com.ruwalabs.saludya.shared.interfaces.rest.transform.ResponseEntityAssembler;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST controller for reassignment offers.
 *
 * <p>Exposes the patient-facing endpoints of the reassignment flow: listing the pending
 * offers of a candidate and accepting/rejecting them. The rest of the flow (triggering
 * the chain, expiring offers, marking no-shows) is driven internally by events and the
 * scheduler.</p>
 */
@RestController
@RequestMapping("/api/v1/reassignment-offers")
public class ReassignmentOffersController {

    private final ReassignmentCommandService reassignmentCommandService;
    private final ReassignmentQueryService reassignmentQueryService;

    public ReassignmentOffersController(
            ReassignmentCommandService reassignmentCommandService,
            ReassignmentQueryService reassignmentQueryService) {
        this.reassignmentCommandService = reassignmentCommandService;
        this.reassignmentQueryService = reassignmentQueryService;
    }

    /**
     * Lists the pending offers of a candidate appointment.
     *
     * @param appointmentId the candidate patient's appointment identifier
     * @return the list of pending offers for that appointment
     */
    @GetMapping("/pending")
    public List<ReassignmentOfferResource> getPendingOffers(@RequestParam Long appointmentId) {
        return reassignmentQueryService.handle(new GetPendingOffersByAppointmentQuery(appointmentId))
                .stream()
                .map(ReassignmentOfferResourceAssembler::toResource)
                .toList();
    }

    /**
     * Accepts a reassignment offer.
     *
     * @param id the offer identifier
     * @return the accepted offer, or an error response
     */
    @PostMapping("/{id}/accept")
    public ResponseEntity<?> acceptOffer(@PathVariable Long id) {
        return ResponseEntityAssembler.toResponseEntityFromResult(
                reassignmentCommandService.acceptReassignment(new AcceptReassignmentCommand(id)),
                ReassignmentOfferResourceAssembler::toResource,
                HttpStatus.OK);
    }

    /**
     * Rejects a reassignment offer.
     *
     * @param id the offer identifier
     * @return the rejected offer, or an error response
     */
    @PostMapping("/{id}/reject")
    public ResponseEntity<?> rejectOffer(@PathVariable Long id) {
        return ResponseEntityAssembler.toResponseEntityFromResult(
                reassignmentCommandService.rejectReassignment(new RejectReassignmentCommand(id)),
                ReassignmentOfferResourceAssembler::toResource,
                HttpStatus.OK);
    }
}
