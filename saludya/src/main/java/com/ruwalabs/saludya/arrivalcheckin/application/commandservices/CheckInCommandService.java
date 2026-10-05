package com.ruwalabs.saludya.arrivalcheckin.application.commandservices;

import com.ruwalabs.saludya.arrivalcheckin.application.commands.DeclareAbsenceCommand;
import com.ruwalabs.saludya.arrivalcheckin.application.commands.RegisterCheckInByBookingCodeCommand;
import com.ruwalabs.saludya.arrivalcheckin.application.commands.RegisterCheckInCommand;
import com.ruwalabs.saludya.arrivalcheckin.application.model.CheckInResult;
import com.ruwalabs.saludya.arrivalcheckin.domain.model.entities.QueueEntry;
import com.ruwalabs.saludya.shared.application.result.ApplicationError;
import com.ruwalabs.saludya.shared.application.result.Result;

/**
 * Command service for check-ins, absences and the attention lifecycle.
 */
public interface CheckInCommandService {

    Result<CheckInResult, ApplicationError> registerCheckIn(RegisterCheckInCommand command);

    Result<CheckInResult, ApplicationError> registerCheckInByBookingCode(
            RegisterCheckInByBookingCodeCommand command);

    Result<Void, ApplicationError> declareAbsence(DeclareAbsenceCommand command);

    /**
     * Detects entries that exceeded the post-call tolerance and marks them absent.
     *
     * @return the number of entries marked as absent
     */
    int detectAbsences();

    /**
     * Starts the attention of a called queue entry.
     *
     * @param queueEntryId the queue entry identifier
     * @return the updated queue entry
     */
    Result<QueueEntry, ApplicationError> startAttention(Long queueEntryId);

    /**
     * Finishes the attention of a queue entry (marks it attended).
     *
     * @param queueEntryId the queue entry identifier
     * @return the updated queue entry
     */
    Result<QueueEntry, ApplicationError> finishAttention(Long queueEntryId);
}
