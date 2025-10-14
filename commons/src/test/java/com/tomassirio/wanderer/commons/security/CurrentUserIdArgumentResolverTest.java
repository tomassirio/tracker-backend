package com.tomassirio.wanderer.commons.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.server.ResponseStatusException;

class CurrentUserIdArgumentResolverTest {

    private JwtUtils jwtUtils;
    private CurrentUserIdArgumentResolver resolver;

    @SuppressWarnings("unused")
    static class TestController {
        public void uuidParam(@CurrentUserId java.util.UUID id) {}

        public void stringParam(@CurrentUserId String id) {}

        public void noAnnotation(UUID id) {}

        public void unsupportedParam(@CurrentUserId Integer id) {}
    }

    @BeforeEach
    void setup() {
        jwtUtils = mock(JwtUtils.class);
        resolver = new CurrentUserIdArgumentResolver(jwtUtils);
    }

    private MethodParameter paramFor(String methodName, Class<?> paramType)
            throws NoSuchMethodException {
        Method m = TestController.class.getMethod(methodName, paramType);
        return new MethodParameter(m, 0);
    }

    @Test
    void supportsParameter_detectsAnnotatedUuidAndString() throws Exception {
        MethodParameter uuidParam = paramFor("uuidParam", UUID.class);
        MethodParameter stringParam = paramFor("stringParam", String.class);
        MethodParameter noAnn = paramFor("noAnnotation", UUID.class);

        assertTrue(resolver.supportsParameter(uuidParam));
        assertTrue(resolver.supportsParameter(stringParam));
        assertFalse(resolver.supportsParameter(noAnn));
    }

    @Test
    void resolveArgument_returnsUuidWhenParamIsUuid() throws Exception {
        MethodParameter uuidParam = paramFor("uuidParam", UUID.class);
        MockHttpServletRequest servlet = new MockHttpServletRequest();
        ServletWebRequest webRequest = new ServletWebRequest(servlet);
        when(jwtUtils.getUserIdFromAuthorizationHeader("Bearer token"))
                .thenReturn(UUID.fromString("00000000-0000-0000-0000-000000000001"));
        servlet.addHeader("Authorization", "Bearer token");

        Object resolved =
                resolver.resolveArgument(
                        uuidParam,
                        new ModelAndViewContainer(),
                        webRequest,
                        mock(WebDataBinderFactory.class));
        assertEquals(UUID.fromString("00000000-0000-0000-0000-000000000001"), resolved);
    }

    @Test
    void resolveArgument_returnsStringWhenParamIsString() throws Exception {
        MethodParameter stringParam = paramFor("stringParam", String.class);
        MockHttpServletRequest servlet = new MockHttpServletRequest();
        ServletWebRequest webRequest = new ServletWebRequest(servlet);
        when(jwtUtils.getUserIdFromAuthorizationHeader("Bearer token"))
                .thenReturn(UUID.fromString("00000000-0000-0000-0000-000000000002"));
        servlet.addHeader("Authorization", "Bearer token");

        Object resolved =
                resolver.resolveArgument(
                        stringParam,
                        new ModelAndViewContainer(),
                        webRequest,
                        mock(WebDataBinderFactory.class));
        assertEquals("00000000-0000-0000-0000-000000000002", resolved);
    }

    @Test
    void resolveArgument_missingHeader_throws401() throws Exception {
        MethodParameter uuidParam = paramFor("uuidParam", UUID.class);
        MockHttpServletRequest servlet = new MockHttpServletRequest();
        ServletWebRequest webRequest = new ServletWebRequest(servlet);

        assertThrows(
                ResponseStatusException.class,
                () ->
                        resolver.resolveArgument(
                                uuidParam,
                                new ModelAndViewContainer(),
                                webRequest,
                                mock(WebDataBinderFactory.class)));
    }

    @Test
    void resolveArgument_unsupportedParam_throws500() throws Exception {
        MethodParameter unsupportedParam = paramFor("unsupportedParam", Integer.class);
        MockHttpServletRequest servlet = new MockHttpServletRequest();
        ServletWebRequest webRequest = new ServletWebRequest(servlet);
        servlet.addHeader("Authorization", "Bearer token");
        when(jwtUtils.getUserIdFromAuthorizationHeader("Bearer token"))
                .thenReturn(UUID.fromString("00000000-0000-0000-0000-000000000003"));

        ResponseStatusException ex =
                assertThrows(
                        ResponseStatusException.class,
                        () ->
                                resolver.resolveArgument(
                                        unsupportedParam,
                                        new ModelAndViewContainer(),
                                        webRequest,
                                        mock(WebDataBinderFactory.class)));
        // verify it's internal server error
        assertEquals(500, ex.getStatusCode().value());
    }
}
