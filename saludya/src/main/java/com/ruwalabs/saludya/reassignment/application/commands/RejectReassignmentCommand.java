package com.ruwalabs.saludya.reassignment.application.commands;

import java.time.Instant;

public record RejectReassignmentCommand(
        Long offerId,
        Long appointmentId,
        Instant rejectedAt
) {
}
