package com.ruwalabs.saludya.iam.infrastructure.tokens;

import com.ruwalabs.saludya.iam.domain.services.TokenService;
import com.ruwalabs.saludya.iam.domain.model.aggregates.UserAccount;
import com.ruwalabs.saludya.iam.domain.model.enums.Role;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.core.*;
import org.springframework.stereotype.Service;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.*;
import java.util.UUID;
@Service
public class TokenServiceImpl implements TokenService {
    private final JwtEncoder encoder;private final JwtDecoder decoder;private final String issuer;private final Clock clock;
    public TokenServiceImpl(@Value("${application.jwt.secret}") String secret,@Value("${iam.jwt.issuer:saludya}") String issuer,Clock clock) {
        if(secret==null||secret.getBytes(StandardCharsets.UTF_8).length<32)
            throw new IllegalArgumentException("JWT secret must contain at least 32 bytes");
        this.issuer=issuer;this.clock=clock;var key=new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8),"HmacSHA256");
        encoder=NimbusJwtEncoder.withSecretKey(key).build();
        var jwtDecoder=NimbusJwtDecoder.withSecretKey(key).macAlgorithm(MacAlgorithm.HS256).build();
        var timestamp=new JwtTimestampValidator(Duration.ZERO);timestamp.setClock(clock);
        jwtDecoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(timestamp,new JwtIssuerValidator(issuer)));
        decoder=jwtDecoder;
    }
    public String issue(UserAccount u,UUID id,Instant expiresAt) {
        var claims=JwtClaimsSet.builder().issuer(issuer).subject(u.getId().toString()).issuedAt(clock.instant())
                .expiresAt(expiresAt).id(id.toString()).claim("role",u.getRole().name()).build();
        return encoder.encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).type("JWT").build(),claims)).getTokenValue();
    }
    public TokenClaims verify(String token) {
        var jwt=decoder.decode(token);
        return new TokenClaims(Long.valueOf(jwt.getSubject()),Role.valueOf(jwt.getClaimAsString("role")),
                UUID.fromString(jwt.getId()),jwt.getExpiresAt());
    }
}
