package com.ruwalabs.saludya.reassignment.application.internal.commandservices;

import com.ruwalabs.saludya.reassignment.application.commands.AcceptReassignmentCommand;
import com.ruwalabs.saludya.reassignment.application.commands.ExpireReassignmentCommand;
import com.ruwalabs.saludya.reassignment.application.commands.RejectReassignmentCommand;
import com.ruwalabs.saludya.reassignment.application.commands.SendReassignmentOfferCommand;
import com.ruwalabs.saludya.reassignment.application.commandservices.ReassignmentOfferCommandService;
import com.ruwalabs.saludya.reassignment.application.internal.outboundservices.acl.HospitalConfigurationService;
import com.ruwalabs.saludya.reassignment.domain.model.aggregates.ReassignmentOffer;
import com.ruwalabs.saludya.reassignment.domain.repositories.ReassignmentOfferRepository;
import com.ruwalabs.saludya.shared.application.result.ApplicationError;
import com.ruwalabs.saludya.shared.application.result.Result;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;

@Service
@Transactional
public class ReassignmentOfferCommandServiceImpl implements ReassignmentOfferCommandService {

    private final ReassignmentOfferRepository repository;
    private final HospitalConfigurationService hospitalConfig;

    public ReassignmentOfferCommandServiceImpl(ReassignmentOfferRepository repository, HospitalConfigurationService hospitalConfig) {
        this.repository = repository;
        this.hospitalConfig = hospitalConfig;
    }

    @Override
    public Result<Long, ApplicationError> handle(SendReassignmentOfferCommand command) {

        var timeOutMin = hospitalConfig.reassignmentResponseTimeOutMinutes();
        var expiresAt = Instant.now().plus(Duration.ofMinutes(timeOutMin));
        var offer = ReassignmentOffer.offer(
                command.appointmentId(),
                command.originalAppointmentId(),
                command.freedTimeSlotId(),
                command.expiresAt()
        );

        repository.save(offer);
        
        return null;
    }

    @Override
    public Result<Long, ApplicationError> handle(AcceptReassignmentCommand command) {

        var offer = this.repository.findById(command.offerId());

        if (offer.isEmpty())throw new IllegalStateException("Offer not found");

        var offerEntity = offer.get();

        offerEntity.accept();

        this.repository.save(offerEntity);

        return null;
    }

    @Override
    public Result<Long, ApplicationError> handle(RejectReassignmentCommand command) {

        var offer = this.repository.findById(command.offerId());

        if (offer.isEmpty())throw new IllegalStateException("Offer not found");

        var offerEntity = offer.get();

        offerEntity.reject();

        this.repository.save(offerEntity);

        return null;
    }

    @Override
    public Result<Long, ApplicationError> handle(ExpireReassignmentCommand command) {
        return null;
    }
}
