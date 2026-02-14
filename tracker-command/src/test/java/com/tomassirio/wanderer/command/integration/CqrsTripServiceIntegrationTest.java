package com.tomassirio.wanderer.command.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import com.tomassirio.wanderer.command.dto.TripCreationRequest;
import com.tomassirio.wanderer.command.repository.TripRepository;
import com.tomassirio.wanderer.command.repository.UserRepository;
import com.tomassirio.wanderer.command.service.TripService;
import com.tomassirio.wanderer.commons.BaseIntegrationTest;
import com.tomassirio.wanderer.commons.domain.Trip;
import com.tomassirio.wanderer.commons.domain.TripVisibility;
import com.tomassirio.wanderer.commons.domain.User;
import com.tomassirio.wanderer.commons.dto.TripDTO;
import java.time.Duration;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Integration test for CQRS Trip Service implementation.
 *
 * <p>Tests the full flow: Command → Event → Persistence → WebSocket
 */
@SpringBootTest
@ActiveProfiles("test")
class CqrsTripServiceIntegrationTest extends BaseIntegrationTest {

    @Autowired private TripService tripService;

    @Autowired private TripRepository tripRepository;

    @Autowired private UserRepository userRepository;

    private UUID testUserId;

    @BeforeEach
    void setUp() {
        // Create a test user
        User testUser = User.builder().username("testuser-cqrs").build();
        testUser = userRepository.save(testUser);
        testUserId = testUser.getId();
    }

    @Test
    void createTrip_shouldEventuallyPersistToDatabase() {
        // Given
        TripCreationRequest request =
                new TripCreationRequest("CQRS Test Trip", TripVisibility.PUBLIC);

        // When - Command executes immediately
        TripDTO result = tripService.createTrip(testUserId, request);

        // Then - Command returns immediately with generated ID
        assertThat(result).isNotNull();
        assertThat(result.id()).isNotNull();
        assertThat(result.name()).isEqualTo("CQRS Test Trip");

        // And - Eventually the trip is persisted by the event listener
        UUID tripId = UUID.fromString(result.id());
        await().atMost(Duration.ofSeconds(5))
                .untilAsserted(
                        () -> {
                            Trip persistedTrip = tripRepository.findById(tripId).orElse(null);
                            assertThat(persistedTrip).isNotNull();
                            assertThat(persistedTrip.getName()).isEqualTo("CQRS Test Trip");
                            assertThat(persistedTrip.getTripSettings()).isNotNull();
                            assertThat(persistedTrip.getTripSettings().getVisibility())
                                    .isEqualTo(TripVisibility.PUBLIC);
                        });
    }
}
