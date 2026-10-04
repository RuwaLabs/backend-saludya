package com.ruwalabs.saludya.arrivalcheckin.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * Resource to register a check-in from a scanned QR token.
 */
@Schema(name = "CheckInQrResource", description = "QR token scanned from the patient's app")
public record CheckInQrResource(
        @NotBlank String qrToken) {
}
