package com.ruwalabs.saludya.iam.infrastructure.hashing;

import com.ruwalabs.saludya.iam.domain.services.HashingService;
import com.ruwalabs.saludya.iam.domain.model.valueobjects.PasswordHash;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import java.nio.charset.StandardCharsets;
@Service
public class BCryptHashingService implements HashingService {
    private final BCryptPasswordEncoder encoder=new BCryptPasswordEncoder(12);
    public PasswordHash hash(String password) {
        if (password==null||password.length()<8||password.getBytes(StandardCharsets.UTF_8).length>72
                || !password.matches("(?s).*[A-Za-z].*") || !password.matches("(?s).*[0-9].*"))
            throw new IllegalArgumentException("Password requires at least 8 characters, a letter and a number, and at most 72 UTF-8 bytes");
        return new PasswordHash(encoder.encode(password));
    }
    public boolean matches(String password,PasswordHash hash) {
        if(password==null||password.getBytes(StandardCharsets.UTF_8).length>72) return false;
        return encoder.matches(password,hash.value());
    }
}
