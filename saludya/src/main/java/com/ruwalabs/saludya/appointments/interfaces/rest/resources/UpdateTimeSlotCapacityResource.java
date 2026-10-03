package com.ruwalabs.saludya.appointments.interfaces.rest.resources;

import jakarta.validation.constraints.Positive;

/**
 * Request payload for updating the capacity of a time slot.
 */
public record UpdateTimeSlotCapacityResource(
        @Positive(message = "maxCapacity must be positive")
        int maxCapacity) {
}
