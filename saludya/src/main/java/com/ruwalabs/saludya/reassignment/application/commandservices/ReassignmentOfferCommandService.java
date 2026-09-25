package com.ruwalabs.saludya.reassignment.application.commandservices;

import com.ruwalabs.saludya.reassignment.application.commands.AcceptReassignmentCommand;
import com.ruwalabs.saludya.reassignment.application.commands.ExpireReassignmentCommand;
import com.ruwalabs.saludya.reassignment.application.commands.RejectReassignmentCommand;
import com.ruwalabs.saludya.reassignment.application.commands.SendReassignmentOfferCommand;
import com.ruwalabs.saludya.shared.application.result.ApplicationError;
import com.ruwalabs.saludya.shared.application.result.Result;

public interface ReassignmentOfferCommandService {

    Result<Long, ApplicationError> handle(SendReassignmentOfferCommand command);
    Result<Long, ApplicationError> handle(AcceptReassignmentCommand command);
    Result<Long, ApplicationError> handle(RejectReassignmentCommand command);
    Result<Long, ApplicationError> handle(ExpireReassignmentCommand command);
}
