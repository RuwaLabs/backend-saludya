package com.ruwalabs.saludya.iam.application.commandservices;

import com.ruwalabs.saludya.iam.application.commands.*;
import com.ruwalabs.saludya.iam.domain.model.entities.PatientMinor;
public interface PatientCommandService {
    PatientMinor linkMinor(LinkMinorCommand command);
    void unlinkMinor(UnlinkMinorCommand command);
}
