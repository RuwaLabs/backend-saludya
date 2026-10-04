package com.ruwalabs.saludya.arrivalcheckin.application.commandservices;

import com.ruwalabs.saludya.arrivalcheckin.application.commands.CallNextPatientCommand;
import com.ruwalabs.saludya.arrivalcheckin.domain.model.entities.QueueEntry;
import com.ruwalabs.saludya.shared.application.result.ApplicationError;
import com.ruwalabs.saludya.shared.application.result.Result;

/**
 * Command service for the attendance queue.
 */
public interface QueueCommandService {

    /**
     * Calls the next waiting patient in a queue.
     *
     * @param command the call-next command
     * @return the called queue entry
     */
    Result<QueueEntry, ApplicationError> callNextPatient(CallNextPatientCommand command);
}
