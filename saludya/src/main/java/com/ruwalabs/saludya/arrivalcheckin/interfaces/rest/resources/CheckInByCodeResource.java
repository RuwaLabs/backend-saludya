package com.ruwalabs.saludya.arrivalcheckin.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * Resource to register a check-in from the reservation code (manual fallback).
 */
@Schema(name = "CheckInByCodeResource", description = "Reservation code entered manually by admission staff")
public record CheckInByCodeResource(
        @NotBlank String bookingCode) {
}
