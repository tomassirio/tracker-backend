package com.tomassirio.wanderer.command.handler;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tomassirio.wanderer.command.event.TripPlanDeletedEvent;
import com.tomassirio.wanderer.commons.domain.TripPlan;
import jakarta.persistence.EntityManager;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TripPlanDeletedEventHandlerTest {

    @Mock private EntityManager entityManager;

    @InjectMocks private TripPlanDeletedEventHandler handler;

    @Test
    void handle_shouldDeleteTripPlan() {
        UUID tripPlanId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        TripPlan tripPlan = TripPlan.builder().id(tripPlanId).build();

        TripPlanDeletedEvent event =
                TripPlanDeletedEvent.builder().tripPlanId(tripPlanId).userId(userId).build();

        when(entityManager.find(TripPlan.class, tripPlanId)).thenReturn(tripPlan);

        handler.handle(event);

        verify(entityManager).remove(tripPlan);
    }
}
