package com.ruwalabs.saludya.iam.application.commandservices;

import com.ruwalabs.saludya.iam.application.commands.*;
import com.ruwalabs.saludya.iam.application.results.*;
import com.ruwalabs.saludya.iam.domain.model.aggregates.Patient;
public interface UserAccountCommandService {
    Patient registerPatient(RegisterPatientCommand command);
    AuthResult login(LoginCommand command);
    void logout(LogoutCommand command);
    void recoverPassword(RecoverPasswordCommand command);
    void resetPassword(ResetPasswordCommand command);
    void changePassword(ChangePasswordCommand command);
    AccountProfile updateProfile(UpdateProfileCommand command);
    AccountProfile createStaffAccount(CreateStaffAccountCommand command);
}
