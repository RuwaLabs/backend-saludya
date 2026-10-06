package com.ruwalabs.saludya.iam;

import com.ruwalabs.saludya.iam.infrastructure.authorization.AuthRateLimitFilter;
import com.ruwalabs.saludya.iam.infrastructure.identity.ApiPeruIdentityGateway;
import com.ruwalabs.saludya.iam.infrastructure.identity.UnavailableIdentityGateway;
import com.ruwalabs.saludya.iam.infrastructure.notifications.NotificationCipher;
import com.ruwalabs.saludya.iam.infrastructure.hashing.BCryptHashingService;
import com.ruwalabs.saludya.iam.application.internal.IdentityVerificationService;
import com.ruwalabs.saludya.iam.domain.model.valueobjects.Dni;
import com.ruwalabs.saludya.iam.domain.model.exceptions.IamException;
import com.ruwalabs.saludya.iam.domain.services.IdentityGateway;
import com.sun.net.httpserver.HttpServer;
import org.springframework.mock.web.*;
import org.junit.jupiter.api.Test;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.*;
import java.util.*;
import static org.assertj.core.api.Assertions.*;

class IamSecurityTests {
    @Test void encryptedNotificationsUseRandomNoncesAndAuthenticateTheirContents() {
        var cipher=new NotificationCipher("c2FsdWR5YS1kZXYta2V5LW5vdC1mb3ItcHJvZHVjdGk=");
        String first=cipher.encrypt("a private recovery link"),second=cipher.encrypt("a private recovery link");
        assertThat(first).isNotEqualTo(second);
        assertThat(cipher.decrypt(first)).isEqualTo("a private recovery link");
        byte[] tampered=Base64.getDecoder().decode(first);tampered[tampered.length-1]^=1;
        assertThatThrownBy(()->cipher.decrypt(Base64.getEncoder().encodeToString(tampered))).isInstanceOf(IllegalStateException.class);
    }
    @Test void bcryptRejectsPasswordsExceedingItsUtf8ByteLimit() {
        var hashing=new BCryptHashingService();
        assertThatThrownBy(()->hashing.hash("é".repeat(40)+"A1")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(()->hashing.hash("a".repeat(73)+"1")).isInstanceOf(IllegalArgumentException.class);
    }
    @Test void perIpLimiterRejectsExcessSensitiveRequestsAndAllowsThemAfterTheWindow() throws Exception {
        var clock=new IamIntegrationTests.MutableClock();var filter=new AuthRateLimitFilter(clock,2,true);
        for(int i=0;i<3;i++) {
            var request=new MockHttpServletRequest("POST","/api/v1/user-accounts/login");request.setRemoteAddr("127.0.0.1");
            var response=new MockHttpServletResponse();
            filter.doFilter(request,response,(req,res)->((jakarta.servlet.http.HttpServletResponse)res).setStatus(204));
            assertThat(response.getStatus()).isEqualTo(i<2?204:429);
            if(i==2) assertThat(response.getHeader("Retry-After")).isEqualTo("60");
        }
        clock.advance(Duration.ofMinutes(1));
        var response=new MockHttpServletResponse();
        filter.doFilter(new MockHttpServletRequest("POST","/api/v1/user-accounts/login"),response,(req,res)->((jakarta.servlet.http.HttpServletResponse)res).setStatus(204));
        assertThat(response.getStatus()).isEqualTo(204);
    }
    @Test void adulthoodUsesTheDeclaredBirthDateAndTheLimaCalendarBoundary() {
        var clock=Clock.fixed(Instant.parse("2026-10-03T03:00:00Z"),ZoneOffset.UTC);
        var service=new IdentityVerificationService(dni->null,clock);
        assertThat(service.today()).isEqualTo(LocalDate.parse("2026-10-02"));
        assertThatThrownBy(()->service.requireAdult(LocalDate.parse("2008-10-03"))).isInstanceOf(IamException.class);
        var nextDay=new IdentityVerificationService(dni->null,Clock.fixed(Instant.parse("2026-10-03T05:00:00Z"),ZoneOffset.UTC));
        assertThatCode(()->nextDay.requireAdult(LocalDate.parse("2008-10-03"))).doesNotThrowAnyException();
    }
    @Test void trustedNamesCanBeAccentAndOrderNormalizedButADifferentNameIsRejected() {
        var identity=new IdentityGateway.OfficialIdentity(new Dni("71234821"),"Lucía","Torres",null,Set.of());
        var service=new IdentityVerificationService(dni->identity,Clock.systemUTC());
        assertThatCode(()->service.verify("71234821","  LUCIA  ","torres")).doesNotThrowAnyException();
        assertThatCode(()->service.verify("71234821","Torres","Lucía")).doesNotThrowAnyException();
        assertThatThrownBy(()->service.verify("71234821","Lucía","Ramírez")).isInstanceOf(IamException.class);
    }
    @Test void existenceCheckDistinguishesAMissingDniFromAProviderOutage() {
        var service=new IdentityVerificationService(dni -> {
            if(dni.value().equals("71234821")) return new IdentityGateway.OfficialIdentity(new Dni("71234821"),"Lucía","Torres",null,Set.of());
            throw new IamException(404,"IAM_DNI_NOT_FOUND","El DNI no existe");
        },Clock.systemUTC());
        assertThat(service.exists("71234821")).isTrue();
        assertThat(service.exists("00000000")).isFalse();
        var unavailable=new IdentityVerificationService(dni->{ throw new IamException(503,"IAM_IDENTITY_UNAVAILABLE","down"); },Clock.systemUTC());
        assertThatThrownBy(()->unavailable.exists("71234821")).isInstanceOfSatisfying(IamException.class,e->assertThat(e.getStatus()).isEqualTo(503));
    }
    @Test void anUnconfiguredIdentityGatewayFailsClosed() {
        assertThatThrownBy(()->new UnavailableIdentityGateway().lookup(new Dni("71234821")))
                .isInstanceOfSatisfying(IamException.class,e->assertThat(e.getStatus()).isEqualTo(503));
    }
    @Test void apiPeruConfigurationRejectsInsecureRemoteUrlsAndFakeLocalhostHosts() {
        assertThatThrownBy(()->new ApiPeruIdentityGateway("http://identity.example.test/dni","")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(()->new ApiPeruIdentityGateway("http://localhost.attacker.test/dni","")).isInstanceOf(IllegalArgumentException.class);
    }
    @Test void apiPeruGatewayHandlesFoundMissingAndUnavailableResponses() throws Exception {
        var server=HttpServer.create(new InetSocketAddress("localhost",0),0);
        server.createContext("/dni",exchange->{
            String body=new String(exchange.getRequestBody().readAllBytes(),StandardCharsets.UTF_8);
            int status=body.contains("44556677")?200:body.contains("00000000")?404:503;
            String response=switch(status) {
                case 200 -> "{\"success\":true,\"code\":\"found\",\"data\":{\"numero\":\"44556677\",\"nombre_completo\":\"PEREZ GARCIA JUAN CARLOS\",\"nombres\":\"JUAN CARLOS\",\"apellido_paterno\":\"PEREZ\",\"apellido_materno\":\"GARCIA\",\"codigo_verificacion\":3}}";
                case 404 -> "{\"success\":false,\"code\":\"document_not_found\",\"message\":\"El DNI no existe\"}";
                default -> "{\"success\":false,\"code\":\"upstream_unavailable\",\"message\":\"Unavailable\"}";
            };
            byte[] bytes=response.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type","application/json");
            exchange.sendResponseHeaders(status,bytes.length);exchange.getResponseBody().write(bytes);exchange.close();
        });
        server.start();
        try {
            var gateway=new ApiPeruIdentityGateway("http://localhost:"+server.getAddress().getPort()+"/dni","test-provider-key");
            var identity=gateway.lookup(new Dni("44556677"));
            assertThat(identity.name()).isEqualTo("Juan Carlos");
            assertThat(identity.lastname()).isEqualTo("Perez Garcia");
            assertThat(identity.birthDate()).isNull();
            assertThatThrownBy(()->gateway.lookup(new Dni("00000000")))
                    .isInstanceOfSatisfying(IamException.class,e->{assertThat(e.getStatus()).isEqualTo(404);assertThat(e.getCode()).isEqualTo("IAM_DNI_NOT_FOUND");});
            assertThatThrownBy(()->gateway.lookup(new Dni("99999999")))
                    .isInstanceOfSatisfying(IamException.class,e->assertThat(e.getStatus()).isEqualTo(503));
        } finally { server.stop(0); }
    }
    @Test void incompleteApiPeruResponsesCannotApproveRegistration() throws Exception {
        var server=HttpServer.create(new InetSocketAddress("localhost",0),0);
        server.createContext("/",exchange->{
            byte[] bytes="{\"success\":true,\"data\":null}".getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type","application/json");exchange.sendResponseHeaders(200,bytes.length);
            exchange.getResponseBody().write(bytes);exchange.close();
        });
        server.start();
        try {
            var gateway=new ApiPeruIdentityGateway("http://localhost:"+server.getAddress().getPort()+"/dni","");
            assertThatThrownBy(()->gateway.lookup(new Dni("71234821"))).isInstanceOfSatisfying(IamException.class,e->assertThat(e.getStatus()).isEqualTo(503));
        } finally { server.stop(0); }
    }
}
