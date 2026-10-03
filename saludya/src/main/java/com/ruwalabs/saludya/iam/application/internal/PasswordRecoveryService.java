package com.ruwalabs.saludya.iam.application.internal;

import com.ruwalabs.saludya.iam.domain.repositories.*;
import com.ruwalabs.saludya.iam.domain.services.*;
import com.ruwalabs.saludya.iam.domain.model.entities.PasswordResetToken;
import com.ruwalabs.saludya.iam.domain.model.aggregates.UserAccount;
import com.ruwalabs.saludya.iam.domain.model.exceptions.IamException;
import com.ruwalabs.saludya.iam.domain.model.valueobjects.Email;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Value;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.time.*;
import java.util.*;
@Service @Transactional
public class PasswordRecoveryService {
    private final UserAccountRepository users;private final PasswordResetTokenRepository tokens;
    private final SessionRepository sessions;private final HashingService hashing;private final NotificationGateway notifications;
    private final Clock clock;private final String resetUrl;
    public PasswordRecoveryService(UserAccountRepository users,PasswordResetTokenRepository tokens,SessionRepository sessions,
            HashingService hashing,NotificationGateway notifications,Clock clock,
            @Value("${iam.password-reset.url}") String resetUrl) {
        this.users=users;this.tokens=tokens;this.sessions=sessions;this.hashing=hashing;this.notifications=notifications;
        this.clock=clock;this.resetUrl=resetUrl;
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
    public void invalidateOutstandingLinks(Long userId) {
        tokens.invalidateByUserId(userId,clock.instant());
    }
    private static IamException invalidToken() {
        return new IamException(422,"IAM_RESET_TOKEN_INVALID","The recovery link has expired or is invalid. Request a new link");
    }
}
