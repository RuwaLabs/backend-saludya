package com.ruwalabs.saludya.reassignment.application.commands;

import java.time.Instant;

public record AcceptReassignmentCommand(
        Long offerId,
        Long appointmentId,
        Instant acceptedAt
) {
}
