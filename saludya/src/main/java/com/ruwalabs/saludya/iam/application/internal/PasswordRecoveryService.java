package com.ruwalabs.saludya.iam.application.internal;

import com.ruwalabs.saludya.iam.domain.repositories.*;
import com.ruwalabs.saludya.iam.domain.services.*;
import com.ruwalabs.saludya.iam.domain.model.entities.PasswordResetToken;
import com.ruwalabs.saludya.iam.domain.model.aggregates.UserAccount;
import com.ruwalabs.saludya.iam.domain.model.exceptions.IamException;
import com.ruwalabs.saludya.iam.domain.model.valueobjects.Email;
import com.ruwalabs.saludya.iam.application.results.PasswordRecoveryChallengeResult;
import com.ruwalabs.saludya.iam.application.results.PasswordResetGrantResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Value;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.time.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
@Service @Transactional
public class PasswordRecoveryService {
    private final UserAccountRepository users;private final PasswordResetTokenRepository tokens;
    private final SessionRepository sessions;private final HashingService hashing;private final NotificationGateway notifications;
    private final Clock clock;private final String resetUrl;
    private final EmailVerificationService emailVerification;
    private static final String CODE_PURPOSE="password-recovery";
    private static final Duration CHALLENGE_TTL=Duration.ofMinutes(15);
    private static final Duration GRANT_TTL=Duration.ofMinutes(10);
    private static final SecureRandom RANDOM=new SecureRandom();
    private record RecoveryChallenge(String email,Long userId,Instant expiresAt) { }
    private record ResetGrant(Long userId,Instant expiresAt) { }
    private final Map<String,RecoveryChallenge> recoveryChallenges=new ConcurrentHashMap<>();
    private final Map<String,ResetGrant> resetGrants=new ConcurrentHashMap<>();
    public PasswordRecoveryService(UserAccountRepository users,PasswordResetTokenRepository tokens,SessionRepository sessions,
            HashingService hashing,NotificationGateway notifications,Clock clock,
            @Value("${iam.password-reset.url}") String resetUrl,EmailVerificationService emailVerification) {
        this.users=users;this.tokens=tokens;this.sessions=sessions;this.hashing=hashing;this.notifications=notifications;
        this.clock=clock;this.resetUrl=resetUrl;this.emailVerification=emailVerification;
        var endpoint=java.net.URI.create(resetUrl);
        boolean secure="https".equals(endpoint.getScheme());
        boolean local="http".equals(endpoint.getScheme())&&"localhost".equals(endpoint.getHost());
        if (endpoint.getHost()==null||endpoint.getUserInfo()!=null||endpoint.getFragment()!=null||(!secure&&!local))
            throw new IllegalArgumentException("Password reset URL must use HTTPS");
    }
    public void request(String email) {
        users.findByEmail(new Email(email).value()).filter(UserAccount::isActive)
                .ifPresent(u->sendLink(u,"Recupera tu contraseña de SaludYa","Recibimos una solicitud para cambiar tu contraseña."));
    }
    /** Starts an OTP recovery without disclosing whether the email is registered. */
    public synchronized PasswordRecoveryChallengeResult requestCode(String rawEmail) {
        var email=new Email(rawEmail).value();
        recoveryChallenges.entrySet().removeIf(e->e.getValue().expiresAt().isBefore(clock.instant())
                ||e.getValue().email().equals(email));
        resetGrants.entrySet().removeIf(e->e.getValue().expiresAt().isBefore(clock.instant()));
        var account=users.findByEmail(email).filter(UserAccount::isActive).orElse(null);
        if(account!=null) resetGrants.entrySet().removeIf(e->e.getValue().userId().equals(account.getId()));
        var challengeId=UUID.randomUUID().toString();var expires=clock.instant().plus(CHALLENGE_TTL);
        recoveryChallenges.put(challengeId,new RecoveryChallenge(email,account==null?null:account.getId(),expires));
        if(account!=null) emailVerification.requestCode(CODE_PURPOSE,email,"Código para restablecer tu contraseña",
                "Tu código para restablecer la contraseña de SaludYa es: {code}");
        return new PasswordRecoveryChallengeResult(challengeId,maskEmail(email),expires);
    }
    /** Consumes the emailed OTP and issues a short-lived, single-use password-reset grant. */
    public synchronized PasswordResetGrantResult verifyRecoveryCode(String challengeId,String code) {
        var challenge=challengeId==null?null:recoveryChallenges.get(challengeId);
        if(challenge==null) throw invalidRecoveryCode();
        if(!challenge.expiresAt().isAfter(clock.instant())) {
            recoveryChallenges.remove(challengeId);throw invalidRecoveryCode();
        }
        emailVerification.verify(CODE_PURPOSE,challenge.email(),code);
        recoveryChallenges.remove(challengeId);
        if(challenge.userId()==null) throw invalidRecoveryCode();
        var account=users.findById(challenge.userId()).filter(UserAccount::isActive).orElseThrow(PasswordRecoveryService::invalidRecoveryCode);
        var raw=Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes());
        var expires=clock.instant().plus(GRANT_TTL);
        resetGrants.put(digest(raw),new ResetGrant(account.getId(),expires));
        return new PasswordResetGrantResult(raw,expires);
    }
    /** Applies a new password only after the email code was verified. */
    public synchronized void resetWithGrant(String raw,String password,String confirm) {
        if(raw==null||!raw.matches("[A-Za-z0-9_-]{43}")) throw invalidToken();
        if(!Objects.equals(password,confirm)) throw new IamException(422,"IAM_PASSWORD_MISMATCH","Passwords do not match");
        var key=digest(raw);var grant=resetGrants.get(key);
        if(grant==null||!grant.expiresAt().isAfter(clock.instant())) { resetGrants.remove(key);throw invalidToken(); }
        var user=users.lockById(grant.userId()).filter(UserAccount::isActive).orElseThrow(PasswordRecoveryService::invalidToken);
        user.changePassword(hashing.hash(password));users.save(user);
        tokens.invalidateByUserId(user.getId(),clock.instant());sessions.revokeByUserId(user.getId(),clock.instant());
        resetGrants.remove(key);
        resetGrants.entrySet().removeIf(e->e.getValue().userId().equals(user.getId()));
        recoveryChallenges.entrySet().removeIf(e->Objects.equals(e.getValue().userId(),user.getId()));
        notifications.enqueue(user.getEmail().value(),"Contraseña actualizada","Tu contraseña de SaludYa cambió y se cerraron las sesiones anteriores.");
    }
    public void sendStaffInvitation(UserAccount user) {
        sendLink(user,"Tu cuenta de personal de SaludYa","Tu cuenta de Personal de Admisión está lista. Define tu contraseña para acceder.");
    }
    private void sendLink(UserAccount user,String subject,String message) {
        var locked=users.lockById(user.getId()).orElseThrow(IamException::notFound);
        tokens.invalidateByUserId(locked.getId(),clock.instant());
        byte[] bytes=new byte[32];new SecureRandom().nextBytes(bytes);
        String raw=Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        tokens.save(new PasswordResetToken(UUID.randomUUID(),locked.getId(),digest(raw),
                clock.instant().plus(Duration.ofMinutes(15)),null));
        notifications.enqueue(locked.getEmail().value(),subject,message+"\nCorreo de acceso: "+locked.getEmail().value()
                +"\n"+resetUrl+(resetUrl.contains("?")?"&":"?")+"token="+raw
                +"\nEl enlace es de un solo uso y vence en 15 minutos. Si no solicitaste este cambio, ignora este correo.");
    }
    public void reset(String raw,String password,String confirm) {
        if (raw==null||!raw.matches("[A-Za-z0-9_-]{43}")) throw invalidToken();
        if (!Objects.equals(password,confirm)) throw new IamException(422,"IAM_PASSWORD_MISMATCH","Passwords do not match");
        // Read only the owner ID before locking. Loading the entity here would cache stale usedAt values.
        var userId=tokens.findOwnerByHash(digest(raw)).orElseThrow(PasswordRecoveryService::invalidToken);
        var user=users.lockById(userId).orElseThrow(PasswordRecoveryService::invalidToken);
        var token=tokens.lockByHash(digest(raw)).orElseThrow(PasswordRecoveryService::invalidToken);
        if (!token.usable(clock.instant())) throw invalidToken();
        if (!user.isActive()) throw invalidToken();
        user.changePassword(hashing.hash(password));users.save(user);
        tokens.invalidateByUserId(user.getId(),clock.instant());sessions.revokeByUserId(user.getId(),clock.instant());
        notifications.enqueue(user.getEmail().value(),"Contraseña actualizada","Tu contraseña de SaludYa cambió y se cerraron las sesiones anteriores.");
    }
    public static String digest(String token) {
        try { return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8))); }
        catch (NoSuchAlgorithmException ex) { throw new IllegalStateException(ex); }
    }
    private static byte[] randomBytes() { byte[] bytes=new byte[32];RANDOM.nextBytes(bytes);return bytes; }
    private static String maskEmail(String email) {
        int at=email.indexOf('@');if(at<=0)return "***";
        return email.charAt(0)+"***"+email.substring(at);
    }
    public void invalidateOutstandingLinks(Long userId) {
        tokens.invalidateByUserId(userId,clock.instant());
    }
    private static IamException invalidToken() {
        return new IamException(422,"IAM_RESET_TOKEN_INVALID","The recovery link has expired or is invalid. Request a new link");
    }
    private static IamException invalidRecoveryCode() {
        return new IamException(422,"IAM_INVALID_EMAIL_CODE","The recovery code is invalid or expired");
    }
}
