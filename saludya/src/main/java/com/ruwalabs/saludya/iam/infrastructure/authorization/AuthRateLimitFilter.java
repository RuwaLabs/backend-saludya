package com.ruwalabs.saludya.iam.infrastructure.authorization;

import org.springframework.web.filter.OncePerRequestFilter;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.time.Clock;
import java.util.*;
/** Bounded per-process limiter. Deployment gateways should enforce a shared limit across replicas. */
public class AuthRateLimitFilter extends OncePerRequestFilter {
    private final Map<String,Window> windows=new HashMap<>();private final Clock clock;private final int max;private final boolean enabled;
    private record Window(long start,int count) {}
    public AuthRateLimitFilter(Clock clock,int max,boolean enabled) { this.clock=clock;this.max=max;this.enabled=enabled; }
    protected void doFilterInternal(HttpServletRequest request,HttpServletResponse response,FilterChain chain) throws ServletException,IOException {
        var path=request.getRequestURI();
        boolean sensitive=request.getMethod().equals("POST")&&(path.equals("/api/v1/user-accounts")||path.endsWith("/login")
                ||path.endsWith("/login/verify")||path.endsWith("/login/resend")
                ||path.endsWith("/send-verification-code")
                ||path.endsWith("/recover-password")||path.endsWith("/reset-password")||path.startsWith("/api/v1/identity-verifications")
                ||path.equals("/api/v1/account-recovery-requests"));
        if(enabled&&sensitive&&!allow(request.getRemoteAddr()+":"+path)) {
            response.setHeader("Retry-After","60");
            BearerAuthorizationRequestFilter.writeError(response,429,"IAM_RATE_LIMITED","Too many requests. Try again later");return;
        }
        chain.doFilter(request,response);
    }
    private synchronized boolean allow(String key) {
        long now=clock.millis();windows.entrySet().removeIf(e->now-e.getValue().start()>=60000);
        var w=windows.get(key);
        if(w==null) { if(windows.size()>=10000) return false;windows.put(key,new Window(now,1));return true; }
        if(w.count()>=max) return false;windows.put(key,new Window(w.start(),w.count()+1));return true;
    }
}
