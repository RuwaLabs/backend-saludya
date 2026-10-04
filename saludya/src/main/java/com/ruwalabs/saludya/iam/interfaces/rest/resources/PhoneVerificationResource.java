package com.ruwalabs.saludya.iam.interfaces.rest.resources;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * Resource to request an SMS verification code.
 */
public record PhoneVerificationResource(
        @NotBlank @Pattern(regexp = "\\+?[0-9]{9,15}") String phone) {
}
