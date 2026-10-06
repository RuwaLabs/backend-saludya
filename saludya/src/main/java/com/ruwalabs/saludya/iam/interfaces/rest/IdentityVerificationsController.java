package com.ruwalabs.saludya.iam.interfaces.rest;

import com.ruwalabs.saludya.iam.application.internal.IdentityVerificationService;
import com.ruwalabs.saludya.iam.interfaces.rest.resources.IdentityExistsResource;
import com.ruwalabs.saludya.iam.interfaces.rest.resources.VerifyIdentityResource;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Map;
@RestController @RequestMapping("/api/v1/identity-verifications") @Tag(name="IAM - Identity verification")
public class IdentityVerificationsController {
    private final IdentityVerificationService identities;
    public IdentityVerificationsController(IdentityVerificationService identities) { this.identities=identities; }
    @PostMapping @SecurityRequirements @Operation(summary="Verify that a DNI exists and the full name matches the official record")
    public Map<String,Boolean> verify(@Valid @RequestBody VerifyIdentityResource r) {
        identities.verify(r.dni(),r.name(),r.lastname());return Map.of("verified",true);
    }
    @PostMapping("/exists") @SecurityRequirements @Operation(summary="Check whether a DNI is known to the identity provider")
    public Map<String,Boolean> exists(@Valid @RequestBody IdentityExistsResource r) {
        return Map.of("exists",identities.exists(r.dni()));
    }
}
