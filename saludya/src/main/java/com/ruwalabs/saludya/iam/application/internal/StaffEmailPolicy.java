package com.ruwalabs.saludya.iam.application.internal;

import com.ruwalabs.saludya.iam.domain.model.exceptions.IamException;
import com.ruwalabs.saludya.iam.domain.model.valueobjects.Email;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.stream.Collectors;

/** Hospital operators configure the corporate domains permitted for admission staff. */
@Service
public class StaffEmailPolicy {
    private final Set<String> allowedDomains;

    public StaffEmailPolicy(@Value("${iam.staff.allowed-email-domains}") String domains) {
        allowedDomains=Arrays.stream(domains.split(",")).map(String::trim).filter(s->!s.isEmpty())
                .map(s->s.toLowerCase(Locale.ROOT)).collect(Collectors.toUnmodifiableSet());
        if(allowedDomains.isEmpty()) throw new IllegalArgumentException("Configure at least one corporate staff email domain");
    }

    public void requireCorporateEmail(Email email) {
        String domain=email.value().substring(email.value().lastIndexOf('@')+1);
        if(!allowedDomains.contains(domain))
            throw new IamException(422,"IAM_CORPORATE_EMAIL_REQUIRED","Use the hospital corporate email address for staff");
    }
}
