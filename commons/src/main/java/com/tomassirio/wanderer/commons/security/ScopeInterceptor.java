package com.tomassirio.wanderer.commons.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.http.HttpStatus;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.HandlerInterceptor;

public class ScopeInterceptor implements HandlerInterceptor {

    private final JwtUtils jwtUtils;

    public ScopeInterceptor(JwtUtils jwtUtils) {
        this.jwtUtils = jwtUtils;
    }

    @Override
    public boolean preHandle(
            HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        HandlerMethod hm = (HandlerMethod) handler;
        RequiredScope requiredAnn =
                AnnotatedElementUtils.findMergedAnnotation(hm.getMethod(), RequiredScope.class);
        if (requiredAnn == null) {
            requiredAnn =
                    AnnotatedElementUtils.findMergedAnnotation(
                            hm.getBeanType(), RequiredScope.class);
        }

        Scope scopeAnn = null;
        if (requiredAnn == null) {
            scopeAnn = AnnotatedElementUtils.findMergedAnnotation(hm.getMethod(), Scope.class);
            if (scopeAnn == null) {
                scopeAnn =
                        AnnotatedElementUtils.findMergedAnnotation(hm.getBeanType(), Scope.class);
            }
        }

        if (requiredAnn == null && scopeAnn == null) {
            return true;
        }

        String needed = requiredAnn != null ? requiredAnn.value() : scopeAnn.value();

        String auth = request.getHeader("Authorization");
        if (auth == null || auth.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED, "Missing Authorization header");
        }

        String prefix = "Bearer ";
        String token = auth.startsWith(prefix) ? auth.substring(prefix.length()) : auth;

        Map<String, Object> payload = jwtUtils.parsePayload(token);
        List<String> scopes = jwtUtils.getScopesFromClaims(payload);
        boolean ok = scopes.stream().anyMatch(s -> s.equalsIgnoreCase(needed));
        if (!ok) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "Required scope not present: " + needed);
        }

        return true;
    }
}
