package com.tomassirio.wanderer.commons.security;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.server.ResponseStatusException;

class ScopeInterceptorTest {

    private JwtUtils jwtUtils;
    private ScopeInterceptor interceptor;
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    static class TestController {
        @Scope("login")
        public void protectedEndpoint() {}

        public void publicEndpoint() {}
    }

    @BeforeEach
    void setup() {
        jwtUtils = mock(JwtUtils.class);
        interceptor = new ScopeInterceptor(jwtUtils);
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
    }

    private HandlerMethod handlerFor(String methodName) throws NoSuchMethodException {
        Method m = TestController.class.getMethod(methodName);
        return new HandlerMethod(new TestController(), m);
    }

    @Test
    void whenNoScopeAnnotation_shouldAllowEvenWithoutAuth() throws Exception {
        HandlerMethod handler = handlerFor("publicEndpoint");
        // No Authorization header
        assertDoesNotThrow(() -> interceptor.preHandle(request, response, handler));
    }

    @Test
    void whenProtectedAndNoAuthorization_shouldThrow401() throws Exception {
        HandlerMethod handler = handlerFor("protectedEndpoint");
        // No Authorization header
        ResponseStatusException ex =
                assertThrows(
                        ResponseStatusException.class,
                        () -> interceptor.preHandle(request, response, handler));
        assertEquals(HttpStatus.UNAUTHORIZED.value(), ex.getStatusCode().value());
    }

    @Test
    void whenProtectedAndTokenWithoutScope_shouldThrow403() throws Exception {
        HandlerMethod handler = handlerFor("protectedEndpoint");
        String token = "dummy-token";
        request.addHeader("Authorization", "Bearer " + token);

        when(jwtUtils.parsePayload(token))
                .thenReturn(Map.of("sub", "00000000-0000-0000-0000-000000000000"));
        when(jwtUtils.getScopesFromClaims(anyMap())).thenReturn(List.of());

        ResponseStatusException ex =
                assertThrows(
                        ResponseStatusException.class,
                        () -> interceptor.preHandle(request, response, handler));
        assertEquals(HttpStatus.FORBIDDEN.value(), ex.getStatusCode().value());
    }

    @Test
    void whenProtectedAndTokenHasScope_shouldAllow() throws Exception {
        HandlerMethod handler = handlerFor("protectedEndpoint");
        String token = "dummy-token";
        request.addHeader("Authorization", "Bearer " + token);

        Map<String, Object> payload = Map.of("sub", "00000000-0000-0000-0000-000000000000");
        when(jwtUtils.parsePayload(token)).thenReturn(payload);
        when(jwtUtils.getScopesFromClaims(anyMap())).thenReturn(List.of("login", "other"));

        boolean allowed = interceptor.preHandle(request, response, handler);
        assertEquals(true, allowed);
    }
}
