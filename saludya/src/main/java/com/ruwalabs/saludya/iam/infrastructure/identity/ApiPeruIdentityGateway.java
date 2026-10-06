package com.ruwalabs.saludya.iam.infrastructure.identity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ruwalabs.saludya.iam.domain.services.IdentityGateway;
import com.ruwalabs.saludya.iam.domain.model.valueobjects.Dni;
import com.ruwalabs.saludya.iam.domain.model.exceptions.IamException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import java.net.URI;
import java.net.http.HttpClient;
import java.time.Duration;
import java.util.*;

/**
 * Adapter for the ApiPeru.dev DNI lookup service (https://apiperu.dev).
 *
 * <p>The provider only returns the document number and the person's full name; it does not
 * expose the birth date or guardianship information, so those remain self-declared by the
 * registrant and are never verified against this provider.</p>
 */
@Service @ConditionalOnProperty(name="iam.identity.mode",havingValue="apiperu")
@Slf4j
public class ApiPeruIdentityGateway implements IdentityGateway {

    private final RestClient client;
    private final String url;

    public ApiPeruIdentityGateway(@Value("${iam.identity.url:https://api.apiperu.dev/dni}") String url,
                                  @Value("${iam.identity.api-key:}") String token) {
        URI endpoint = URI.create(url);
        boolean secure = "https".equals(endpoint.getScheme());
        boolean local = "http".equals(endpoint.getScheme()) && "localhost".equals(endpoint.getHost());
        if (endpoint.getHost() == null || endpoint.getUserInfo() != null || (!secure && !local)) {
            throw new IllegalArgumentException("Identity URL must use HTTPS");
        }
        this.url = url;
        if (token.isBlank()) {
            log.warn("ApiPeru identity gateway has no API token configured (APIPERU_TOKEN); DNI verification will fail until it is provided");
        } else {
            log.info("ApiPeru identity gateway configured for {}", url);
        }
        var http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
        var factory = new org.springframework.http.client.JdkClientHttpRequestFactory(http);
        factory.setReadTimeout(Duration.ofSeconds(10));
        var builder = RestClient.builder().requestFactory(factory);
        if (!token.isBlank()) {
            builder.defaultHeader("Authorization", "Bearer " + token);
        }
        this.client = builder.build();
    }

    @Override
    public OfficialIdentity lookup(Dni dni) {
        try {
            var response = client.post().uri(URI.create(url))
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .body(Map.of("dni", dni.value()))
                    .retrieve()
                    .body(ApiPeruResponse.class);
            if (response == null || response.data() == null
                    || response.data().nombres() == null || response.data().nombres().isBlank()
                    || response.data().apellidoPaterno() == null || response.data().apellidoPaterno().isBlank()) {
                throw new IamException(503, "IAM_IDENTITY_UNAVAILABLE", "Identity verification is temporarily unavailable");
            }
            var data = response.data();
            var name = titleCase(data.nombres());
            var lastname = titleCase(joinSurnames(data.apellidoPaterno(), data.apellidoMaterno()));
            return new OfficialIdentity(new Dni(dni.value()), name, lastname, null, Set.of());
        } catch (HttpClientErrorException.Unauthorized | HttpClientErrorException.Forbidden ex) {
            log.error("ApiPeru rejected the configured credentials; check the APIPERU_TOKEN value");
            throw new IamException(503, "IAM_IDENTITY_CONFIGURATION", "The identity provider credentials are not configured");
        } catch (HttpClientErrorException.NotFound ex) {
            throw new IamException(404, "IAM_DNI_NOT_FOUND", "El DNI no existe");
        } catch (RestClientException ex) {
            throw new IamException(503, "IAM_IDENTITY_UNAVAILABLE", "Identity verification is temporarily unavailable");
        }
    }

    private static String joinSurnames(String paternal, String maternal) {
        if (maternal == null || maternal.isBlank()) {
            return paternal;
        }
        return paternal + " " + maternal;
    }

    private static String titleCase(String value) {
        if (value == null) {
            return null;
        }
        var words = value.strip().toLowerCase(Locale.ROOT).split("\\s+");
        var builder = new StringBuilder();
        for (var word : words) {
            if (word.isEmpty()) {
                continue;
            }
            if (!builder.isEmpty()) {
                builder.append(' ');
            }
            builder.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
        }
        return builder.toString();
    }

    public record ApiPeruResponse(boolean success, String code, Data data) {
        public record Data(String numero,
                           @JsonProperty("nombre_completo") String nombreCompleto,
                           String nombres,
                           @JsonProperty("apellido_paterno") String apellidoPaterno,
                           @JsonProperty("apellido_materno") String apellidoMaterno,
                           @JsonProperty("codigo_verificacion") Integer codigoVerificacion) { }
    }
}
