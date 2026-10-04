package com.ruwalabs.saludya.iam.interfaces.rest;

import com.ruwalabs.saludya.iam.application.commands.UpdateProfileCommand;
import com.ruwalabs.saludya.iam.application.commandservices.UserAccountCommandService;
import com.ruwalabs.saludya.iam.application.queryservices.PatientQueryService;
import com.ruwalabs.saludya.iam.domain.model.entities.PatientMinor;
import com.ruwalabs.saludya.iam.domain.model.exceptions.IamException;
import com.ruwalabs.saludya.iam.infrastructure.authorization.IamPrincipal;
import com.ruwalabs.saludya.iam.interfaces.rest.resources.*;
import com.ruwalabs.saludya.iam.interfaces.rest.transform.PatientResourceAssembler;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
@RestController @RequestMapping("/api/v1/patients") @Tag(name="IAM - Patients")
public class PatientsController {
    private final PatientQueryService queries; private final UserAccountCommandService accounts;
    public PatientsController(PatientQueryService queries,UserAccountCommandService accounts) { this.queries=queries;this.accounts=accounts; }
    @GetMapping("/{id}") @Operation(summary="Read your patient profile or linked minor; staff may read patient identities")
    public PatientResource get(@AuthenticationPrincipal IamPrincipal p,@PathVariable Long id) {
        return PatientResourceAssembler.from(queries.getById(p.userId(),id));
    }
    @PutMapping("/{id}") @Operation(summary="Update your own patient contact information")
    public PatientResource update(@AuthenticationPrincipal IamPrincipal p,@PathVariable Long id,@Valid @RequestBody UpdateProfileResource r) {
        var patient=queries.getById(p.userId(),id);
        if (!p.userId().equals(patient.getUserId())) throw IamException.forbidden();
        accounts.updateProfile(new UpdateProfileCommand(p.userId(),patient.getUserId(),r.email(),r.phone()));
        return PatientResourceAssembler.from(queries.getById(p.userId(),id));
    }
    @GetMapping("/{id}/minors") @Operation(summary="List minor links belonging to your adult patient profile")
    public List<PatientMinor> minors(@AuthenticationPrincipal IamPrincipal p,@PathVariable Long id) {
        return queries.getMinorsByTutor(p.userId(),id);
    }
}
