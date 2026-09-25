package com.ruwalabs.saludya.reassignment.application.commands;

import java.time.Instant;

public record ExpireReassignmentCommand(
        Long offerId,
        Long appointmentId,
        Instant expiredAt
) {
}
