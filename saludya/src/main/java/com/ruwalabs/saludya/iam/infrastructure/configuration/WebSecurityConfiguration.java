package com.ruwalabs.saludya.iam.infrastructure.configuration;

import com.ruwalabs.saludya.iam.infrastructure.authorization.*;
import com.ruwalabs.saludya.iam.domain.repositories.*;
import com.ruwalabs.saludya.iam.domain.services.TokenService;
import org.springframework.context.annotation.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.http.HttpMethod;
import org.springframework.web.cors.*;
import java.time.Clock;
import java.util.*;
@Configuration @EnableMethodSecurity
public class WebSecurityConfiguration {
    @Bean public SecurityFilterChain iamSecurityFilterChain(HttpSecurity http,TokenService tokens,UserAccountRepository users,
            SessionRepository sessions,Clock clock,@Value("${iam.rate-limit.requests-per-minute:30}") int rateLimit,
            @Value("${iam.rate-limit.enabled:true}") boolean rateEnabled) throws Exception {
        var bearer=new BearerAuthorizationRequestFilter(tokens,users,sessions,clock);
        var limiter=new AuthRateLimitFilter(clock,rateLimit,rateEnabled);
        return http.csrf(csrf->csrf.disable()).cors(cors->{}).sessionManagement(s->s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .formLogin(c->c.disable()).httpBasic(c->c.disable()).logout(c->c.disable())
                .authorizeHttpRequests(a->a
                    .requestMatchers(HttpMethod.OPTIONS,"/**").permitAll()
                    .requestMatchers("/v3/api-docs/**","/swagger-ui/**","/swagger-ui.html").permitAll()
                    .requestMatchers(HttpMethod.POST,"/api/v1/user-accounts","/api/v1/user-accounts/login",
                        "/api/v1/user-accounts/recover-password","/api/v1/user-accounts/reset-password",
                        "/api/v1/identity-verifications","/api/v1/account-recovery-requests").permitAll()
                    .requestMatchers(HttpMethod.GET,"/api/v1/account-recovery-requests/support").permitAll()
                    .requestMatchers("/api/v1/user-accounts/staff").hasRole("SUPER_ADMIN")
                    .requestMatchers("/api/v1/account-recovery-requests/**").hasRole("SUPER_ADMIN")
                    .requestMatchers("/api/v1/patient-minors/**").hasRole("PATIENT")
                    .requestMatchers("/api/v1/config/**","/api/v1/hospital-configurations/**","/api/v1/configurations/**").hasRole("SUPER_ADMIN")
                    .anyRequest().authenticated())
                .exceptionHandling(e->e.authenticationEntryPoint((req,res,ex)->BearerAuthorizationRequestFilter.writeError(res,401,"IAM_UNAUTHENTICATED","Authentication is required"))
                    .accessDeniedHandler((req,res,ex)->BearerAuthorizationRequestFilter.writeError(res,403,"IAM_ACCESS_DENIED","You do not have permission to access this resource")))
                .addFilterBefore(bearer,UsernamePasswordAuthenticationFilter.class).addFilterBefore(limiter,BearerAuthorizationRequestFilter.class)
                .build();
    }
    @Bean public CorsConfigurationSource corsConfigurationSource(@Value("${iam.cors.allowed-origins:}") String origins) {
        var config=new CorsConfiguration();config.setAllowedOrigins(origins.isBlank()?List.of():Arrays.stream(origins.split(",")).map(String::trim).toList());
        config.setAllowedMethods(List.of("GET","POST","PUT","DELETE","OPTIONS"));config.setAllowedHeaders(List.of("Authorization","Content-Type"));
        config.setAllowCredentials(false);var source=new UrlBasedCorsConfigurationSource();source.registerCorsConfiguration("/**",config);return source;
    }
}
