package com.ruwalabs.saludya.iam.application.results;

/**
 * Credentials produced by an assisted account recovery.
 *
 * @param email    the new (generated) access email set on the account
 * @param password the new (generated) temporary password
 * @param message  human-readable instructions for the operator
 */
public record RecoveryResolution(String email, String password, String message) { }
