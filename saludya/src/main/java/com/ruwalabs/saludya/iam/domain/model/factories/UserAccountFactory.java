package com.ruwalabs.saludya.iam.domain.model.factories;

import com.ruwalabs.saludya.iam.domain.model.aggregates.UserAccount;
import com.ruwalabs.saludya.iam.domain.model.enums.Role;
import com.ruwalabs.saludya.iam.domain.model.valueobjects.*;
import java.time.Instant;
public final class UserAccountFactory {
    private UserAccountFactory() {}
    public static UserAccount createPatientAccount(Email email, PasswordHash hash, Instant now) {
        return new UserAccount(null,email,hash,Role.PATIENT,true,now);
    }
    public static UserAccount createStaffAccount(Email email, PasswordHash hash, Instant now) {
        return new UserAccount(null,email,hash,Role.ADMISSION_STAFF,true,now);
    }
}
