package com.ruwalabs.saludya.arrivalcheckin.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Resource representing a signed QR token.
 */
@Schema(name = "QrTokenResource", description = "Signed JWT to be rendered as a QR code")
public record QrTokenResource(String qrToken) {
}
