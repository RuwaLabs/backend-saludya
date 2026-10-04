package com.ruwalabs.saludya.iam.interfaces.rest.resources;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * Resource to confirm an SMS verification code.
 */
public record PhoneVerificationConfirmResource(
        @NotBlank @Pattern(regexp = "\\+?[0-9]{9,15}") String phone,
        @NotBlank @Pattern(regexp = "[0-9]{6}") String code) {
}
