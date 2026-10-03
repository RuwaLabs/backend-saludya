package com.ruwalabs.saludya.iam.infrastructure.identity;

import com.ruwalabs.saludya.iam.domain.services.IdentityGateway;
import com.ruwalabs.saludya.iam.domain.model.valueobjects.Dni;
import com.ruwalabs.saludya.iam.domain.model.exceptions.IamException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
@Service @ConditionalOnProperty(name="iam.identity.mode",havingValue="unavailable",matchIfMissing=true)
public class UnavailableIdentityGateway implements IdentityGateway {
    public OfficialIdentity lookup(Dni dni) {
        throw new IamException(503,"IAM_IDENTITY_UNAVAILABLE","The identity provider has not been configured");
    }
}
