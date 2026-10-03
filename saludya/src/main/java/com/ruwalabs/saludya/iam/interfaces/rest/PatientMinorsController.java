package com.ruwalabs.saludya.iam.interfaces.rest;

import com.ruwalabs.saludya.iam.application.commands.*;
import com.ruwalabs.saludya.iam.application.commandservices.PatientCommandService;
import com.ruwalabs.saludya.iam.application.queryservices.PatientQueryService;
import com.ruwalabs.saludya.iam.domain.model.entities.PatientMinor;
import com.ruwalabs.saludya.iam.infrastructure.authorization.IamPrincipal;
import com.ruwalabs.saludya.iam.interfaces.rest.resources.LinkMinorResource;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.http.ResponseEntity;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.net.URI;
@RestController @RequestMapping("/api/v1/patient-minors") @Tag(name="IAM - Linked minors")
public class PatientMinorsController {
    private final PatientCommandService commands;private final PatientQueryService queries;
    public PatientMinorsController(PatientCommandService commands,PatientQueryService queries) { this.commands=commands;this.queries=queries; }
    @PostMapping @Operation(summary="Link a verified minor after confirming a trusted guardian relationship")
    public ResponseEntity<PatientMinor> link(@AuthenticationPrincipal IamPrincipal p,@Valid @RequestBody LinkMinorResource r) {
        var link=commands.linkMinor(new LinkMinorCommand(p.userId(),r.dni(),r.name(),r.lastname(),r.birthDate(),r.confirmFiliation()));
        return ResponseEntity.created(URI.create("/api/v1/patient-minors/"+link.id())).body(link);
    }
    @GetMapping("/{id}") public PatientMinor get(@AuthenticationPrincipal IamPrincipal p,@PathVariable Long id) {
        return queries.getLinkById(p.userId(),id);
    }
    @DeleteMapping("/{id}") @Operation(summary="Remove your guardian link while preserving the minor patient and clinical history")
    public ResponseEntity<Void> unlink(@AuthenticationPrincipal IamPrincipal p,@PathVariable Long id) {
        commands.unlinkMinor(new UnlinkMinorCommand(p.userId(),id));return ResponseEntity.noContent().build();
    }
}
