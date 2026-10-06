package com.ruwalabs.saludya.iam.interfaces.rest;

import com.ruwalabs.saludya.iam.application.commands.*;
import com.ruwalabs.saludya.iam.application.commandservices.UserAccountCommandService;
import com.ruwalabs.saludya.iam.application.queryservices.UserAccountQueryService;
import com.ruwalabs.saludya.iam.application.results.*;
import com.ruwalabs.saludya.iam.infrastructure.authorization.IamPrincipal;
import com.ruwalabs.saludya.iam.interfaces.rest.resources.*;
import com.ruwalabs.saludya.iam.interfaces.rest.transform.PatientResourceAssembler;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.http.ResponseEntity;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.net.URI;
import java.util.Map;
@RestController @RequestMapping("/api/v1/user-accounts") @Tag(name="IAM - User accounts")
public class UserAccountsController {
    private final UserAccountCommandService commands; private final UserAccountQueryService queries;
    public UserAccountsController(UserAccountCommandService commands,UserAccountQueryService queries) {
        this.commands=commands;this.queries=queries;
    }
    @PostMapping("/send-verification-code") @SecurityRequirements @Operation(summary="Send an email verification code before registering")
    public ResponseEntity<Void> sendVerificationCode(@Valid @RequestBody SendVerificationCodeResource r) {
        commands.sendVerificationCode(new SendVerificationCodeCommand(r.email())); return ResponseEntity.accepted().build();
    }
    @PostMapping @SecurityRequirements @Operation(summary="Register a verified adult patient using the emailed code")
    public ResponseEntity<PatientResource> register(@Valid @RequestBody RegisterPatientResource r) {
        var p=commands.registerPatient(new RegisterPatientCommand(r.dni(),r.name(),r.lastname(),r.birthDate(),r.phone(),r.email(),r.password(),r.code()));
        return ResponseEntity.created(URI.create("/api/v1/patients/"+p.getId())).body(PatientResourceAssembler.from(p));
    }
    @PostMapping("/login") @SecurityRequirements @Operation(summary="Start sign-in: verify credentials and send an email code")
    public LoginChallengeResult login(@Valid @RequestBody LoginResource r) { return commands.startLogin(new LoginCommand(r.email(),r.password())); }
    @PostMapping("/login/verify") @SecurityRequirements @Operation(summary="Complete sign-in with the email verification code")
    public AuthResult verifyLogin(@Valid @RequestBody VerifyLoginResource r) { return commands.verifyLogin(new VerifyLoginCommand(r.challengeId(),r.code())); }
    @PostMapping("/login/resend") @SecurityRequirements @Operation(summary="Resend the email verification code for a login challenge")
    public ResponseEntity<Void> resendLoginCode(@Valid @RequestBody ResendLoginResource r) {
        commands.resendLoginCode(new ResendLoginCodeCommand(r.challengeId())); return ResponseEntity.accepted().build();
    }
    @PostMapping("/logout") @Operation(summary="Revoke the current bearer session")
    public ResponseEntity<Void> logout(@AuthenticationPrincipal IamPrincipal p) {
        commands.logout(new LogoutCommand(p.sessionId())); return ResponseEntity.noContent().build();
    }
    @PostMapping("/recover-password") @SecurityRequirements @Operation(summary="Request a single-use recovery email")
    public ResponseEntity<Map<String,String>> recover(@Valid @RequestBody RecoverPasswordResource r) {
        commands.recoverPassword(new RecoverPasswordCommand(r.email()));
        return ResponseEntity.accepted().body(Map.of("message","If an active account exists, a recovery email will be sent."));
    }
    @PostMapping("/reset-password") @SecurityRequirements @Operation(summary="Redeem a recovery link and revoke previous sessions")
    public ResponseEntity<Void> reset(@Valid @RequestBody ResetPasswordResource r) {
        commands.resetPassword(new ResetPasswordCommand(r.token(),r.password(),r.confirmPassword()));
        return ResponseEntity.noContent().build();
    }
    @PostMapping("/change-password") @Operation(summary="Change your password using the current password")
    public ResponseEntity<Void> change(@AuthenticationPrincipal IamPrincipal p,@Valid @RequestBody ChangePasswordResource r) {
        commands.changePassword(new ChangePasswordCommand(p.userId(),r.currentPassword(),r.password(),r.confirmPassword()));
        return ResponseEntity.noContent().build();
    }
    @GetMapping("/me") @Operation(summary="Read your account and identity profile")
    public AccountProfile me(@AuthenticationPrincipal IamPrincipal p) { return queries.getById(p.userId(),p.userId()); }
    @GetMapping("/{id}") @Operation(summary="Read your profile; super administrators may read another account")
    public AccountProfile get(@AuthenticationPrincipal IamPrincipal p,@PathVariable Long id) { return queries.getById(p.userId(),id); }
    @PutMapping("/{id}") @Operation(summary="Update only your email and phone; identity and role remain immutable")
    public AccountProfile update(@AuthenticationPrincipal IamPrincipal p,@PathVariable Long id,@Valid @RequestBody UpdateProfileResource r) {
        return commands.updateProfile(new UpdateProfileCommand(p.userId(),id,r.email(),r.phone()));
    }
    @PostMapping("/staff") @Operation(summary="Create admission staff and email a password setup invitation (SUPER_ADMIN)")
    public ResponseEntity<AccountProfile> staff(@AuthenticationPrincipal IamPrincipal p,@Valid @RequestBody CreateStaffAccountResource r) {
        var a=commands.createStaffAccount(new CreateStaffAccountCommand(p.userId(),r.dni(),r.name(),r.lastname(),r.birthDate(),r.phone(),r.email()));
        return ResponseEntity.created(URI.create("/api/v1/user-accounts/"+a.id())).body(a);
    }
}
