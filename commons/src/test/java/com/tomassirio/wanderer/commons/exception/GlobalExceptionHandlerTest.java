package com.tomassirio.wanderer.commons.exception;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @InjectMocks private GlobalExceptionHandler globalExceptionHandler;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        TestController testController = new TestController();
        mockMvc =
                MockMvcBuilders.standaloneSetup(testController)
                        .setControllerAdvice(globalExceptionHandler)
                        .build();
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
                new MethodArgumentNotValidException(null, bindingResult);

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
                new MethodArgumentNotValidException(null, bindingResult);

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
    }
}
