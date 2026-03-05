package com.tomassirio.wanderer.command.service.impl;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import com.tomassirio.wanderer.command.client.WandererAuthClient;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AdminServiceImplTest {

    @Mock private WandererAuthClient wandererAuthClient;

    @InjectMocks private AdminServiceImpl adminService;

    @Test
    void promoteToAdmin_shouldDelegateToAuthClient() {
        UUID userId = UUID.randomUUID();
        doNothing().when(wandererAuthClient).promoteToAdmin(userId);

        adminService.promoteToAdmin(userId);

        verify(wandererAuthClient).promoteToAdmin(userId);
    }

    @Test
    void demoteFromAdmin_shouldDelegateToAuthClient() {
        UUID userId = UUID.randomUUID();
        doNothing().when(wandererAuthClient).demoteFromAdmin(userId);

        adminService.demoteFromAdmin(userId);

        verify(wandererAuthClient).demoteFromAdmin(userId);
    }

    @Test
    void promoteToAdmin_whenAuthServiceFails_shouldPropagateException() {
        UUID userId = UUID.randomUUID();
        doThrow(new RuntimeException("Auth service unavailable"))
                .when(wandererAuthClient)
                .promoteToAdmin(userId);

        assertThatThrownBy(() -> adminService.promoteToAdmin(userId))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Auth service unavailable");
    }
}
