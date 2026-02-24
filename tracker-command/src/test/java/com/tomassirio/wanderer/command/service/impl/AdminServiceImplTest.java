package com.tomassirio.wanderer.command.service.impl;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import com.tomassirio.wanderer.command.client.TrackerAuthClient;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AdminServiceImplTest {

    @Mock private TrackerAuthClient trackerAuthClient;

    @InjectMocks private AdminServiceImpl adminService;

    @Test
    void promoteToAdmin_shouldDelegateToAuthClient() {
        UUID userId = UUID.randomUUID();
        doNothing().when(trackerAuthClient).promoteToAdmin(userId);

        adminService.promoteToAdmin(userId);

        verify(trackerAuthClient).promoteToAdmin(userId);
    }

    @Test
    void demoteFromAdmin_shouldDelegateToAuthClient() {
        UUID userId = UUID.randomUUID();
        doNothing().when(trackerAuthClient).demoteFromAdmin(userId);

        adminService.demoteFromAdmin(userId);

        verify(trackerAuthClient).demoteFromAdmin(userId);
    }

    @Test
    void promoteToAdmin_whenAuthServiceFails_shouldPropagateException() {
        UUID userId = UUID.randomUUID();
        doThrow(new RuntimeException("Auth service unavailable"))
                .when(trackerAuthClient)
                .promoteToAdmin(userId);

        assertThatThrownBy(() -> adminService.promoteToAdmin(userId))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Auth service unavailable");
    }
}
