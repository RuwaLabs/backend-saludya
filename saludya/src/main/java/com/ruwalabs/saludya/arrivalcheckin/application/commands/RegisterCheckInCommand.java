package com.ruwalabs.saludya.arrivalcheckin.application.commands;

/**
 * Command to register a check-in from a scanned QR token.
 *
 * @param qrToken the raw token read from the QR code
 */
public record RegisterCheckInCommand(String qrToken) {
}
