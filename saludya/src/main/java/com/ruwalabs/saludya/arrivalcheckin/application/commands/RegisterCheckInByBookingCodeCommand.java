package com.ruwalabs.saludya.arrivalcheckin.application.commands;

/**
 * Command to register a check-in using the patient's reservation code
 * (used when the QR cannot be scanned).
 *
 * @param bookingCode the reservation code
 */
public record RegisterCheckInByBookingCodeCommand(String bookingCode) {
}
