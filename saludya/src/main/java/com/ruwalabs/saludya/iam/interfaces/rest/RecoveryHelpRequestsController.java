package com.ruwalabs.saludya.iam.interfaces.rest;

import com.ruwalabs.saludya.iam.application.internal.AccountRecoveryHelpService;
import com.ruwalabs.saludya.iam.domain.model.entities.RecoveryHelpRequest;
import com.ruwalabs.saludya.iam.infrastructure.authorization.IamPrincipal;
import com.ruwalabs.saludya.iam.interfaces.rest.resources.RecoveryHelpRequestResource;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.annotation.Value;
import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.*;
@RestController @RequestMapping("/api/v1/account-recovery-requests") @Tag(name="IAM - Assisted recovery")
public class RecoveryHelpRequestsController {
    private final AccountRecoveryHelpService service;private final String instructions,phone;
    public RecoveryHelpRequestsController(AccountRecoveryHelpService service,@Value("${iam.support.instructions}") String instructions,
            @Value("${iam.support.phone:}") String phone) {
        this.service=service;this.instructions=instructions;this.phone=phone;
    }
    @GetMapping("/support") @SecurityRequirements
    public Map<String,String> support() { return Map.of("instructions",instructions,"phone",phone); }
    @PostMapping @SecurityRequirements @Operation(summary="Ask for assisted recovery; this does not grant access or disclose an account")
    public ResponseEntity<Map<String,String>> request(@Valid @RequestBody RecoveryHelpRequestResource r) {
        service.request(r.dni(),r.contactEmail());
        return ResponseEntity.accepted().body(Map.of("message","Request received. An authorized administrator must verify your identity in person.","instructions",instructions,"phone",phone));
    }
    @GetMapping @Operation(summary="List the oldest 100 open assisted recovery requests (SUPER_ADMIN)")
    public List<RecoveryHelpRequest> open(@AuthenticationPrincipal IamPrincipal p) { return service.open(p.userId()); }
    public record ResolveResource(@AssertTrue boolean identityCheckedInPerson) { }
    @PostMapping("/{id}/resolve") @Operation(summary="Approve a recovery after checking the physical DNI; action is audited (SUPER_ADMIN)")
    public ResponseEntity<Void> resolve(@AuthenticationPrincipal IamPrincipal p,@PathVariable UUID id,@Valid @RequestBody ResolveResource r) {
        service.resolve(p.userId(),id,r.identityCheckedInPerson());return ResponseEntity.noContent().build();
    }
}
