package com.tomassirio.wanderer.command.websocket;

import static org.junit.jupiter.api.Assertions.*;

import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.socket.WebSocketSession;

class WebSocketSessionManagerTest {

    private WebSocketSessionManager sessionManager;

    @Mock private WebSocketSession session;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        sessionManager = new WebSocketSessionManager();
    }

    @Test
    void shouldRegisterAndUnregisterSession() {
        // Given
        UUID userId = UUID.randomUUID();
        String sessionId = "test-session";
        org.mockito.Mockito.when(session.getId()).thenReturn(sessionId);
        org.mockito.Mockito.when(session.isOpen()).thenReturn(true);

        // When
        sessionManager.registerSession(session, userId);

        // Then
        assertEquals(1, sessionManager.getActiveSessionsCount());
        assertEquals(userId, sessionManager.getUserId(session));

        // When
        sessionManager.unregisterSession(session);

        // Then
        assertEquals(0, sessionManager.getActiveSessionsCount());
    }

    @Test
    void shouldSubscribeAndUnsubscribeToTopic() {
        // Given
        UUID userId = UUID.randomUUID();
        String sessionId = "test-session";
        String topic = "/topic/trips/" + UUID.randomUUID();
        org.mockito.Mockito.when(session.getId()).thenReturn(sessionId);

        // When
        sessionManager.registerSession(session, userId);
        sessionManager.subscribe(session, topic);

        // Then
        assertEquals(1, sessionManager.getSubscribersCount(topic));

        // When
        sessionManager.unsubscribe(session, topic);

        // Then
        assertEquals(0, sessionManager.getSubscribersCount(topic));
    }

    @Test
    void shouldTrackMultipleSubscribers() {
        // Given
        WebSocketSession session1 = org.mockito.Mockito.mock(WebSocketSession.class);
        WebSocketSession session2 = org.mockito.Mockito.mock(WebSocketSession.class);
        String topic = "/topic/trips/" + UUID.randomUUID();

        org.mockito.Mockito.when(session1.getId()).thenReturn("session-1");
        org.mockito.Mockito.when(session2.getId()).thenReturn("session-2");

        // When
        sessionManager.registerSession(session1, UUID.randomUUID());
        sessionManager.registerSession(session2, UUID.randomUUID());
        sessionManager.subscribe(session1, topic);
        sessionManager.subscribe(session2, topic);

        // Then
        assertEquals(2, sessionManager.getSubscribersCount(topic));
        assertEquals(2, sessionManager.getActiveSessionsCount());
    }
}
