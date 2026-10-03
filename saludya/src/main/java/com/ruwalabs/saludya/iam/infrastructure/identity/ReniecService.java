package com.ruwalabs.saludya.iam.infrastructure.identity;

import com.ruwalabs.saludya.iam.domain.services.IdentityGateway;
import com.ruwalabs.saludya.iam.domain.model.valueobjects.Dni;
import com.ruwalabs.saludya.iam.domain.model.exceptions.IamException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.client.*;
import java.net.URI;
import java.time.*;
import java.net.http.*;
import java.util.Set;
/** Adapter for a trusted identity-provider gateway; its normalized contract is documented in docs/iam.md. */
@Service @ConditionalOnProperty(name="iam.identity.mode",havingValue="remote")
public class ReniecService implements IdentityGateway {
    private final RestClient client;private final String url;
    public ReniecService(@Value("${iam.identity.url:}") String url,@Value("${iam.identity.api-key:}") String key) {
        URI endpoint=URI.create(url.replace("{dni}","00000000"));
        boolean secure="https".equals(endpoint.getScheme());
        boolean local="http".equals(endpoint.getScheme())&&"localhost".equals(endpoint.getHost());
        if(!url.contains("{dni}")||endpoint.getHost()==null||endpoint.getUserInfo()!=null||(!secure&&!local))
            throw new IllegalArgumentException("Identity URL must use HTTPS and contain {dni}");
        this.url=url;
        var http=HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
        var factory=new org.springframework.http.client.JdkClientHttpRequestFactory(http);factory.setReadTimeout(Duration.ofSeconds(10));
        var builder=RestClient.builder().requestFactory(factory);
        if(!key.isBlank()) builder.defaultHeader("Authorization","Bearer "+key);
        client=builder.build();
    }
    public OfficialIdentity lookup(Dni dni) {
        try {
            var r=client.get().uri(URI.create(url.replace("{dni}",dni.value()))).retrieve().body(IdentityResponse.class);
            if(r==null||r.birthDate()==null||r.name()==null||r.lastname()==null||!dni.value().equals(r.dni()))
                throw new IamException(503,"IAM_IDENTITY_UNAVAILABLE","Identity verification is temporarily unavailable");
            return new OfficialIdentity(new Dni(r.dni()),r.name(),r.lastname(),r.birthDate(),r.guardianDnis()==null?Set.of():Set.copyOf(r.guardianDnis()));
        } catch (HttpClientErrorException.NotFound ex) {
            throw new IamException(422,"IAM_IDENTITY_MISMATCH","The identity information does not match the DNI");
        } catch (RestClientException ex) {
            throw new IamException(503,"IAM_IDENTITY_UNAVAILABLE","Identity verification is temporarily unavailable");
        }
    }
    public record IdentityResponse(String dni,String name,String lastname,LocalDate birthDate,Set<String> guardianDnis) {}
}
