package com.ruwalabs.saludya.iam.domain.services;

/**
 * Outbound port for sending SMS messages (phone verification codes).
 */
public interface SmsGateway {

    /**
     * Sends an SMS message to a phone number.
     *
     * @param phone   the destination phone number
     * @param message the message body
     */
    void send(String phone, String message);
}
