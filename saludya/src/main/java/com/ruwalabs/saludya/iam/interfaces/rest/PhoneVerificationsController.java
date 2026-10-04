package com.ruwalabs.saludya.iam.interfaces.rest;

import com.ruwalabs.saludya.iam.application.internal.PhoneVerificationService;
import com.ruwalabs.saludya.iam.interfaces.rest.resources.PhoneVerificationConfirmResource;
import com.ruwalabs.saludya.iam.interfaces.rest.resources.PhoneVerificationResource;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * REST controller for phone (SMS) verification.
 */
@RestController
@RequestMapping("/api/v1/phone-verifications")
@Tag(name = "IAM - Phone verifications", description = "SMS verification of phone numbers")
public class PhoneVerificationsController {

    private final PhoneVerificationService phoneVerificationService;

    public PhoneVerificationsController(PhoneVerificationService phoneVerificationService) {
        this.phoneVerificationService = phoneVerificationService;
    }

    @PostMapping
    @SecurityRequirements
    @Operation(summary = "Request an SMS verification code")
    public ResponseEntity<Map<String, String>> request(@Valid @RequestBody PhoneVerificationResource resource) {
        phoneVerificationService.requestCode(resource.phone());
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(Map.of("message", "If the phone number is valid, a verification code will be sent by SMS."));
    }

    @PostMapping("/confirm")
    @SecurityRequirements
    @Operation(summary = "Confirm the SMS verification code")
    public ResponseEntity<Void> confirm(@Valid @RequestBody PhoneVerificationConfirmResource resource) {
        if (!phoneVerificationService.verify(resource.phone(), resource.code())) {
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).build();
        }
        return ResponseEntity.noContent().build();
    }
}
