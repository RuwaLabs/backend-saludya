package com.ruwalabs.saludya.iam.infrastructure.identity;

import com.ruwalabs.saludya.iam.domain.services.IdentityGateway;
import com.ruwalabs.saludya.iam.domain.model.valueobjects.Dni;
import com.ruwalabs.saludya.iam.domain.model.exceptions.IamException;
import org.springframework.context.annotation.Profile;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.*;
/** Synthetic allow-listed identities for local demonstrations; never a substitute for RENIEC. */
@Service @Profile({"dev","test"})
@ConditionalOnProperty(name="iam.identity.mode",havingValue="fixtures")
public class DevelopmentIdentityGateway implements IdentityGateway {
    private static final Map<String,OfficialIdentity> IDENTITIES=Map.of(
        "71234821",identity("71234821","Lucía","Torres","1995-04-15",Set.of()),
        "70000002",identity("70000002","María","Vega","1992-02-10",Set.of()),
        "87654321",identity("87654321","Diego","Ramos","1990-06-20",Set.of()),
        "30000001",identity("30000001","Carlos","Vega","1980-01-12",Set.of()),
        "87652716",identity("87652716","Mateo","Torres","2018-04-15",Set.of("71234821")),
        "87654218",identity("87654218","Sofía","Torres","2020-08-05",Set.of("71234821")));
    private static OfficialIdentity identity(String dni,String name,String lastname,String birth,Set<String> guardians) {
        return new OfficialIdentity(new Dni(dni),name,lastname,LocalDate.parse(birth),guardians);
    }
    public OfficialIdentity lookup(Dni dni) {
        var identity=IDENTITIES.get(dni.value());
        if(identity==null) throw new IamException(404,"IAM_DNI_NOT_FOUND","El DNI no existe");
        return identity;
    }
}
