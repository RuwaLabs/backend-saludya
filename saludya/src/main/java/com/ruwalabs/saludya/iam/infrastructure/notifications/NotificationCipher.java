package com.ruwalabs.saludya.iam.infrastructure.notifications;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import javax.crypto.*;
import javax.crypto.spec.*;
import java.security.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
@Component
public class NotificationCipher {
    private final SecretKeySpec key;
    public NotificationCipher(@Value("${iam.notifications.encryption-key}") String encoded) {
        byte[] bytes=Base64.getDecoder().decode(encoded);
        if(bytes.length!=32) throw new IllegalArgumentException("Notification encryption key must be Base64-encoded 32 bytes");
        key=new SecretKeySpec(bytes,"AES");
    }
    public String encrypt(String body) {
        try {
            byte[] nonce=new byte[12];new SecureRandom().nextBytes(nonce);
            var cipher=Cipher.getInstance("AES/GCM/NoPadding");cipher.init(Cipher.ENCRYPT_MODE,key,new GCMParameterSpec(128,nonce));
            byte[] encrypted=cipher.doFinal(body.getBytes(StandardCharsets.UTF_8));
            byte[] packed=new byte[nonce.length+encrypted.length];System.arraycopy(nonce,0,packed,0,nonce.length);
            System.arraycopy(encrypted,0,packed,nonce.length,encrypted.length);return Base64.getEncoder().encodeToString(packed);
        } catch(GeneralSecurityException ex) { throw new IllegalStateException("Notification encryption failed",ex); }
    }
    public String decrypt(String encoded) {
        try {
            byte[] packed=Base64.getDecoder().decode(encoded);var cipher=Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE,key,new GCMParameterSpec(128,Arrays.copyOfRange(packed,0,12)));
            return new String(cipher.doFinal(Arrays.copyOfRange(packed,12,packed.length)),StandardCharsets.UTF_8);
        } catch(GeneralSecurityException ex) { throw new IllegalStateException("Notification decryption failed",ex); }
    }
}
