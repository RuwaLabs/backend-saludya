package com.ruwalabs.saludya.reassignment.application.internal.commandservices;

import com.ruwalabs.saludya.reassignment.application.commands.AcceptReassignmentCommand;
import com.ruwalabs.saludya.reassignment.application.commands.RejectReassignmentCommand;
import com.ruwalabs.saludya.reassignment.application.commands.SendReassignmentOfferCommand;
import com.ruwalabs.saludya.reassignment.application.commandservices.ReassignmentCommandService;
import com.ruwalabs.saludya.reassignment.application.internal.outboundservices.acl.AppointmentLookupService;
import com.ruwalabs.saludya.reassignment.application.internal.outboundservices.acl.HospitalConfigurationService;
import com.ruwalabs.saludya.reassignment.domain.model.aggregates.ReassignmentOffer;
import com.ruwalabs.saludya.reassignment.domain.repositories.ReassignmentOfferRepository;
import com.ruwalabs.saludya.shared.application.result.ApplicationError;
import com.ruwalabs.saludya.shared.application.result.Result;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

/**
 * Application service that executes reassignment commands.
 */
@Service
public class ReassignmentCommandServiceImpl implements ReassignmentCommandService {

    private final ReassignmentOfferRepository reassignmentOfferRepository;
    private final AppointmentLookupService appointmentLookupService;
    private final HospitalConfigurationService hospitalConfigurationService;

    public ReassignmentCommandServiceImpl(
            ReassignmentOfferRepository reassignmentOfferRepository,
            AppointmentLookupService appointmentLookupService,
            HospitalConfigurationService hospitalConfigurationService) {
        this.reassignmentOfferRepository = reassignmentOfferRepository;
        this.appointmentLookupService = appointmentLookupService;
        this.hospitalConfigurationService = hospitalConfigurationService;
    }

    @Override
    @Transactional
    public Optional<Long> sendReassignmentOffer(SendReassignmentOfferCommand command) {
        var candidate = appointmentLookupService.findNextCandidateByBookingOrder(
                command.freedTimeSlotId(), command.originalAppointmentId());
        if (candidate.isEmpty()) {
            // No candidate in the booking queue: the slot stays closed.
            return Optional.empty();
        }

        var windowMinutes = hospitalConfigurationService.reassignmentResponseTimeoutMinutes();
        var expiresAt = Instant.now().plus(Duration.ofMinutes(windowMinutes));
        var candidateSlot = appointmentLookupService.timeSlotOfAppointment(candidate.get());

        var offer = ReassignmentOffer.offer(
                candidate.get(),
                command.originalAppointmentId(),
                command.freedTimeSlotId(),
                candidateSlot,
                expiresAt);

        var saved = reassignmentOfferRepository.save(offer);
        return Optional.of(saved.getId());
    }

    @Override
    @Transactional
    public Result<ReassignmentOffer, ApplicationError> acceptReassignment(AcceptReassignmentCommand command) {
        return reassignmentOfferRepository.findById(command.offerId())
                .map(offer -> {
                    if (!offer.isPending()) {
                        return Result.<ReassignmentOffer, ApplicationError>failure(
                                ApplicationError.conflict("ReassignmentOffer", "Offer is not pending"));
                    }
                    if (offer.isExpired()) {
                        return Result.<ReassignmentOffer, ApplicationError>failure(
                                ApplicationError.businessRuleViolation("reassignment-window", "Offer has expired"));
                    }
                    offer.accept();
                    return Result.<ReassignmentOffer, ApplicationError>success(
                            reassignmentOfferRepository.save(offer));
                })
                .orElseGet(() -> Result.failure(
                        ApplicationError.notFound("ReassignmentOffer", command.offerId().toString())));
    }

    @Override
    @Transactional
    public Result<ReassignmentOffer, ApplicationError> rejectReassignment(RejectReassignmentCommand command) {
        return reassignmentOfferRepository.findById(command.offerId())
                .map(offer -> {
                    if (!offer.isPending()) {
                        return Result.<ReassignmentOffer, ApplicationError>failure(
                                ApplicationError.conflict("ReassignmentOffer", "Offer is not pending"));
                    }
                    offer.reject();
                    return Result.<ReassignmentOffer, ApplicationError>success(
                            reassignmentOfferRepository.save(offer));
                })
                .orElseGet(() -> Result.failure(
                        ApplicationError.notFound("ReassignmentOffer", command.offerId().toString())));
    }

    @Override
    @Transactional
    public int detectNoShows() {
        var now = Instant.now();
        var toleranceMinutes = hospitalConfigurationService.checkInToleranceMinutes();
        var graceMinutes = hospitalConfigurationService.reassignmentResponseTimeoutMinutes();
        int count = 0;

        for (var offer : reassignmentOfferRepository.findAllAccepted()) {
            // Safeguard: if the candidate already arrived (present/attended), skip.
            var status = appointmentLookupService.statusOfAppointment(offer.getAppointmentId()).orElse(null);
            if ("CONFIRMED".equals(status) || "ATTENDED".equals(status)) {
                continue;
            }

            // Arrival deadline: destination slot start + check-in tolerance.
            var slotStart = appointmentLookupService.slotStartOfAppointment(offer.getAppointmentId()).orElse(null);
            Instant deadline;
            if (slotStart != null) {
                deadline = slotStart.plus(Duration.ofMinutes(toleranceMinutes));
                // Safeguard: minimum grace from acceptance when the slot already started.
                if (offer.getRespondedAt() != null) {
                    var minimumDeadline = offer.getRespondedAt().plus(Duration.ofMinutes(graceMinutes));
                    if (deadline.isBefore(minimumDeadline)) {
                        deadline = minimumDeadline;
                    }
                }
            } else {
                // Safeguard: fall back to the response window when the slot is unknown.
                deadline = offer.getExpiresAt();
            }

            if (now.isAfter(deadline)) {
                offer.markNoShow();
                reassignmentOfferRepository.save(offer);
                count++;
            }
        }
        return count;
    }
}
