package com.ruwalabs.saludya.arrivalcheckin.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Resource representing a queue position.
 */
@Schema(name = "QueuePositionResource", description = "Current queue position")
public record QueuePositionResource(int position, int totalInQueue) {
}
