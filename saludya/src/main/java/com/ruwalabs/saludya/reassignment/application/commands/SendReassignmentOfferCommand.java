package com.ruwalabs.saludya.reassignment.application.commands;

import java.time.Instant;

public record SendReassignmentOfferCommand(
        Long appointmentId,
        Long originalAppointmentId,
        Long freedTimeSlotId,
        Instant expiresAt
) {
}
