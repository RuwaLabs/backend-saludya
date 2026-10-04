package com.ruwalabs.saludya.iam.infrastructure.authorization;

import com.ruwalabs.saludya.iam.domain.repositories.*;
import com.ruwalabs.saludya.iam.domain.services.TokenService;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtException;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.time.Clock;
import java.util.List;
/** Validates signature, expiry, current account state and persisted session on every request. */
public class BearerAuthorizationRequestFilter extends OncePerRequestFilter {
    private final TokenService tokens;private final UserAccountRepository users;private final SessionRepository sessions;private final Clock clock;
    public BearerAuthorizationRequestFilter(TokenService tokens,UserAccountRepository users,SessionRepository sessions,Clock clock) {
        this.tokens=tokens;this.users=users;this.sessions=sessions;this.clock=clock;
    }
    protected void doFilterInternal(HttpServletRequest request,HttpServletResponse response,FilterChain chain) throws ServletException,IOException {
        String header=request.getHeader("Authorization");
        if(header!=null) {
            try {
                if(!header.startsWith("Bearer ")||header.length()>8192) throw new IllegalArgumentException();
                var c=tokens.verify(header.substring(7));var user=users.findById(c.userId()).orElseThrow(IllegalArgumentException::new);
                var session=sessions.findById(c.sessionId()).orElseThrow(IllegalArgumentException::new);
                if(!user.isActive()||user.getRole()!=c.role()||!session.userId().equals(c.userId())||!session.isValid(clock.instant())
                        || !session.expiresAt().equals(c.expiresAt())) throw new IllegalArgumentException();
                var principal=new IamPrincipal(user.getId(),user.getRole(),c.sessionId());
                SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(principal,null,
                        List.of(new SimpleGrantedAuthority("ROLE_"+user.getRole().name()))));
            } catch(JwtException|IllegalArgumentException ex) {
                SecurityContextHolder.clearContext();writeError(response,401,"IAM_UNAUTHENTICATED","Authentication is required or the session is invalid");return;
            }
        }
        chain.doFilter(request,response);
    }
    public static void writeError(HttpServletResponse response,int status,String code,String message) throws IOException {
        response.setStatus(status);response.setContentType("application/json");response.setCharacterEncoding("UTF-8");
        response.getWriter().write("{\"code\":\""+code+"\",\"message\":\""+message+"\"}");
    }
}
