package com.tomassirio.wanderer.commons.security;

import java.util.UUID;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.server.ResponseStatusException;

public class CurrentUserIdArgumentResolver implements HandlerMethodArgumentResolver {

    private final JwtUtils jwtUtils;

    public CurrentUserIdArgumentResolver(JwtUtils jwtUtils) {
        this.jwtUtils = jwtUtils;
    }

    @Override
    public boolean supportsParameter(@NonNull MethodParameter parameter) {
        return parameter.hasParameterAnnotation(CurrentUserId.class)
                && (UUID.class.isAssignableFrom(parameter.getParameterType())
                        || String.class.isAssignableFrom(parameter.getParameterType()));
    }

    @Override
    public Object resolveArgument(
            @NonNull MethodParameter parameter,
            @Nullable ModelAndViewContainer mavContainer,
            @Nullable NativeWebRequest webRequest,
            @Nullable WebDataBinderFactory binderFactory) {
        if (webRequest == null) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR, "Request context not available");
        }

        CurrentUserId annotation = parameter.getParameterAnnotation(CurrentUserId.class);
        boolean required = annotation != null && annotation.required();

        String auth = webRequest.getHeader("Authorization");
        if (auth == null || auth.isBlank()) {
            if (required) {
                throw new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "Missing Authorization header");
            }
            return null; // Return null for optional user ID
        }

        UUID userId = jwtUtils.getUserIdFromAuthorizationHeader(auth);
        if (UUID.class.isAssignableFrom(parameter.getParameterType())) {
            return userId;
        } else if (String.class.isAssignableFrom(parameter.getParameterType())) {
            return userId.toString();
        }
        throw new ResponseStatusException(
                HttpStatus.INTERNAL_SERVER_ERROR, "Unsupported parameter type for @CurrentUserId");
    }
}
