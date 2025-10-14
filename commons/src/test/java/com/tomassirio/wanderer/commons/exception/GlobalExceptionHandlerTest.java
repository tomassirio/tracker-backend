package com.tomassirio.wanderer.commons.exception;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import jakarta.persistence.EntityNotFoundException;
import java.lang.reflect.Method;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    private MockMvc mockMvc;
    private Level previousLogLevel;

    @InjectMocks private GlobalExceptionHandler globalExceptionHandler;

    @BeforeEach
    void setUp() {
        TestController testController = new TestController();
        mockMvc =
                MockMvcBuilders.standaloneSetup(testController)
                        .setControllerAdvice(globalExceptionHandler)
                        .build();

        // Silence the GlobalExceptionHandler logger for these tests so the handled
        // exception does not produce a noisy stacktrace in the test output.
        Logger logger = (Logger) LoggerFactory.getLogger(GlobalExceptionHandler.class);
        previousLogLevel = logger.getLevel();
        logger.setLevel(Level.OFF);
    }

    @AfterEach
    void tearDown() {
        // Restore previous log level
        Logger logger = (Logger) LoggerFactory.getLogger(GlobalExceptionHandler.class);
        logger.setLevel(previousLogLevel);
    }

    private MethodParameter createDummyMethodParameter() {
        try {
            Method method = TestController.class.getMethod("dummy", String.class);
            return new MethodParameter(method, 0);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void handleValidationExceptions_withMultipleErrors_shouldReturnBadRequestWithAllErrors() {
        // Given
        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.getAllErrors())
                .thenReturn(
                        List.of(
                                new FieldError("object", "name", "Field 'name' is required"),
                                new FieldError("object", "email", "Field 'email' must be valid")));

        MethodArgumentNotValidException exception =
                new MethodArgumentNotValidException(createDummyMethodParameter(), bindingResult);

        // When
        ResponseEntity<Object> response =
                globalExceptionHandler.handleValidationExceptions(exception);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody())
                .isEqualTo("Field 'name' is required, Field 'email' must be valid");
    }

    @Test
    void handleValidationExceptions_withSingleError_shouldReturnBadRequestWithSingleError() {
        // Given
        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.getAllErrors())
                .thenReturn(List.of(new FieldError("object", "field", "Single field error")));

        MethodArgumentNotValidException exception =
                new MethodArgumentNotValidException(createDummyMethodParameter(), bindingResult);

        // When
        ResponseEntity<Object> response =
                globalExceptionHandler.handleValidationExceptions(exception);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo("Single field error");
    }

    @Test
    void handleIllegalArgumentException_shouldReturnBadRequest() {
        // Given
        IllegalArgumentException exception =
                new IllegalArgumentException("Invalid argument provided");

        // When
        ResponseEntity<String> response =
                globalExceptionHandler.handleIllegalArgumentException(exception);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void handleIllegalArgumentException_withCustomMessage_shouldReturnBadRequest() {
        // Given
        IllegalArgumentException exception =
                new IllegalArgumentException("Trip not found with ID: 123");

        // When
        ResponseEntity<String> response =
                globalExceptionHandler.handleIllegalArgumentException(exception);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void handleEntityNotFoundException_shouldReturnNotFound() {
        // Given
        EntityNotFoundException exception = new EntityNotFoundException("Trip not found");

        // When
        ResponseEntity<Void> response =
                globalExceptionHandler.handleEntityNotFoundException(exception);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void handleEntityNotFoundException_withCustomMessage_shouldReturnNotFound() {
        // Given
        EntityNotFoundException exception =
                new EntityNotFoundException("Entity with ID abc-123 does not exist");

        // When
        ResponseEntity<Void> response =
                globalExceptionHandler.handleEntityNotFoundException(exception);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void handleAllUncaughtException_shouldReturnInternalServerError() {
        // Given
        RuntimeException exception = new RuntimeException("Unexpected error occurred");

        // When
        ResponseEntity<String> response =
                globalExceptionHandler.handleAllUncaughtException(exception);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    void handleAccessDeniedException_shouldReturnForbidden() {
        // Given
        AccessDeniedException ex = new AccessDeniedException("forbidden");

        // When
        ResponseEntity<String> response = globalExceptionHandler.handleAccessDeniedException(ex);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).isEqualTo("forbidden");
    }

    @Test
    void handleResponseStatusException_withReason_shouldReturnStatusAndReason() {
        // Given
        ResponseStatusException ex =
                new ResponseStatusException(HttpStatus.CONFLICT, "conflict reason");

        // When
        ResponseEntity<Object> response = globalExceptionHandler.handleResponseStatusException(ex);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isEqualTo("conflict reason");
    }

    @Test
    void handleResponseStatusException_withoutReason_shouldReturnStatusNoBody() {
        // Given
        ResponseStatusException ex = new ResponseStatusException(HttpStatus.CONFLICT);

        // When
        ResponseEntity<Object> response = globalExceptionHandler.handleResponseStatusException(ex);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isNull();
    }

    @Test
    void handleIllegalArgumentException_throughMockMvc_shouldReturnBadRequest() throws Exception {
        // When & Then
        mockMvc.perform(get("/test/illegal-argument-exception")).andExpect(status().isBadRequest());
    }

    @Test
    void handleEntityNotFoundException_throughMockMvc_shouldReturnNotFound() throws Exception {
        // When & Then
        mockMvc.perform(get("/test/entity-not-found-exception")).andExpect(status().isNotFound());
    }

    @Test
    void handleAllUncaughtException_throughMockMvc_shouldReturnInternalServerError()
            throws Exception {
        // When & Then
        mockMvc.perform(get("/test/unexpected-exception"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void handleAccessDeniedException_throughMockMvc_shouldReturnForbidden() throws Exception {
        mockMvc.perform(get("/test/access-denied"))
                .andExpect(status().isForbidden())
                .andExpect(content().string("forbidden"));
    }

    @Test
    void handleResponseStatusException_withReason_throughMockMvc_shouldReturnStatusAndReason()
            throws Exception {
        mockMvc.perform(get("/test/response-status-with-reason"))
                .andExpect(status().isConflict())
                .andExpect(content().string("conflict reason"));
    }

    @Test
    void handleResponseStatusException_withoutReason_throughMockMvc_shouldReturnStatusNoBody()
            throws Exception {
        mockMvc.perform(get("/test/response-status-no-reason"))
                .andExpect(status().isConflict())
                .andExpect(content().string(""));
    }

    // Test controller to simulate exceptions
    @RestController
    static class TestController {

        @GetMapping("/test/illegal-argument-exception")
        public void throwIllegalArgumentException() {
            throw new IllegalArgumentException("Invalid argument provided");
        }

        @GetMapping("/test/entity-not-found-exception")
        public void throwEntityNotFoundException() {
            throw new EntityNotFoundException("Trip not found");
        }

        @GetMapping("/test/unexpected-exception")
        public void throwUnexpectedException() {
            throw new RuntimeException("Unexpected error occurred");
        }

        @GetMapping("/test/access-denied")
        public void throwAccessDenied() {
            throw new AccessDeniedException("forbidden");
        }

        @GetMapping("/test/response-status-with-reason")
        public void throwResponseStatusWithReason() {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "conflict reason");
        }

        @GetMapping("/test/response-status-no-reason")
        public void throwResponseStatusNoReason() {
            throw new ResponseStatusException(HttpStatus.CONFLICT);
        }

        @SuppressWarnings("unused")
        public void dummy(String param) {}
    }
}
