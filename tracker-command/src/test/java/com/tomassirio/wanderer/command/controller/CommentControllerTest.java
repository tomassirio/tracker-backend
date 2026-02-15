package com.tomassirio.wanderer.command.controller;

import static com.tomassirio.wanderer.commons.utils.BaseTestEntityFactory.USER_ID;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.tomassirio.wanderer.command.dto.CommentCreationRequest;
import com.tomassirio.wanderer.command.dto.ReactionRequest;
import com.tomassirio.wanderer.command.service.CommentService;
import com.tomassirio.wanderer.commons.domain.ReactionType;
import com.tomassirio.wanderer.commons.exception.GlobalExceptionHandler;
import com.tomassirio.wanderer.commons.utils.MockMvcTestUtils;
import jakarta.persistence.EntityNotFoundException;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@ExtendWith(MockitoExtension.class)
class CommentControllerTest {

    private static final String TRIPS_BASE_URL = "/api/1/trips";
    private static final String COMMENTS_BASE_URL = "/api/1/comments";
    private static final UUID TRIP_ID = UUID.randomUUID();
    private static final UUID COMMENT_ID = UUID.randomUUID();
    private static final UUID PARENT_COMMENT_ID = UUID.randomUUID();

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock private CommentService commentService;

    @InjectMocks private CommentController commentController;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        mockMvc =
                MockMvcTestUtils.buildMockMvcWithCurrentUserResolver(
                        commentController, new GlobalExceptionHandler());
    }

    // ============ CREATE TOP-LEVEL COMMENT TESTS ============

    @Test
    void createComment_whenValidTopLevelComment_shouldReturnCreatedComment() throws Exception {
        // Given
        CommentCreationRequest request = new CommentCreationRequest("Great trip!", null);

        doReturn(COMMENT_ID)
                .when(commentService)
                .createComment(eq(USER_ID), eq(TRIP_ID), any(CommentCreationRequest.class));

        // When & Then
        mockMvc.perform(
                        post(TRIPS_BASE_URL + "/{tripId}/comments", TRIP_ID)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$").value(COMMENT_ID.toString()));

        verify(commentService)
                .createComment(eq(USER_ID), eq(TRIP_ID), any(CommentCreationRequest.class));
    }

    @Test
    void createComment_whenValidReply_shouldReturnCreatedReply() throws Exception {
        // Given
        CommentCreationRequest request = new CommentCreationRequest("Thanks!", PARENT_COMMENT_ID);

        doReturn(COMMENT_ID)
                .when(commentService)
                .createComment(eq(USER_ID), eq(TRIP_ID), any(CommentCreationRequest.class));

        // When & Then
        mockMvc.perform(
                        post(TRIPS_BASE_URL + "/{tripId}/comments", TRIP_ID)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$").value(COMMENT_ID.toString()));

        verify(commentService)
                .createComment(eq(USER_ID), eq(TRIP_ID), any(CommentCreationRequest.class));
    }

    @Test
    void createComment_whenMessageIsBlank_shouldReturnBadRequest() throws Exception {
        // Given
        CommentCreationRequest request = new CommentCreationRequest("", null);

        // When & Then
        mockMvc.perform(
                        post(TRIPS_BASE_URL + "/{tripId}/comments", TRIP_ID)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createComment_whenMessageIsTooLong_shouldReturnBadRequest() throws Exception {
        // Given - message with more than 1000 characters
        String longMessage = "A".repeat(1001);
        CommentCreationRequest request = new CommentCreationRequest(longMessage, null);

        // When & Then
        mockMvc.perform(
                        post(TRIPS_BASE_URL + "/{tripId}/comments", TRIP_ID)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createComment_whenTripNotFound_shouldReturnNotFound() throws Exception {
        // Given
        CommentCreationRequest request = new CommentCreationRequest("Great trip!", null);

        doThrow(new EntityNotFoundException("Trip not found"))
                .when(commentService)
                .createComment(eq(USER_ID), eq(TRIP_ID), any(CommentCreationRequest.class));

        // When & Then
        mockMvc.perform(
                        post(TRIPS_BASE_URL + "/{tripId}/comments", TRIP_ID)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void createComment_whenReplyingToReply_shouldReturnBadRequest() throws Exception {
        // Given
        CommentCreationRequest request =
                new CommentCreationRequest("Reply to reply", PARENT_COMMENT_ID);

        doThrow(
                        new IllegalArgumentException(
                                "Cannot create a reply to a reply. Maximum nesting depth is 1."))
                .when(commentService)
                .createComment(eq(USER_ID), eq(TRIP_ID), any(CommentCreationRequest.class));

        // When & Then
        mockMvc.perform(
                        post(TRIPS_BASE_URL + "/{tripId}/comments", TRIP_ID)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    // ============ ADD REACTION TO COMMENT TESTS ============

    @Test
    void addReactionToComment_whenValidRequest_shouldReturnUpdatedComment() throws Exception {
        // Given
        ReactionRequest request = new ReactionRequest(ReactionType.HEART);

        doReturn(COMMENT_ID)
                .when(commentService)
                .addReactionToComment(USER_ID, COMMENT_ID, ReactionType.HEART);

        // When & Then
        mockMvc.perform(
                        post(COMMENTS_BASE_URL + "/{commentId}/reactions", COMMENT_ID)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$").value(COMMENT_ID.toString()));

        verify(commentService).addReactionToComment(USER_ID, COMMENT_ID, ReactionType.HEART);
    }

    @Test
    void addReactionToComment_whenReactionTypeIsNull_shouldReturnBadRequest() throws Exception {
        // Given
        String requestBody = "{\"reactionType\": null}";

        // When & Then
        mockMvc.perform(
                        post(COMMENTS_BASE_URL + "/{commentId}/reactions", COMMENT_ID)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    // ============ REMOVE REACTION FROM COMMENT TESTS ============

    @Test
    void removeReactionFromComment_whenValidRequest_shouldReturnUpdatedComment() throws Exception {
        // Given
        ReactionRequest request = new ReactionRequest(ReactionType.HEART);

        doReturn(COMMENT_ID)
                .when(commentService)
                .removeReactionFromComment(USER_ID, COMMENT_ID, ReactionType.HEART);

        // When & Then
        mockMvc.perform(
                        delete(COMMENTS_BASE_URL + "/{commentId}/reactions", COMMENT_ID)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$").value(COMMENT_ID.toString()));

        verify(commentService).removeReactionFromComment(USER_ID, COMMENT_ID, ReactionType.HEART);
    }

    @Test
    void removeReactionFromComment_whenReactionTypeIsNull_shouldReturnBadRequest()
            throws Exception {
        // Given
        String requestBody = "{\"reactionType\": null}";

        // When & Then
        mockMvc.perform(
                        delete(COMMENTS_BASE_URL + "/{commentId}/reactions", COMMENT_ID)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    // ============ ADDITIONAL REACTION TYPE TESTS ============

    @Test
    void addReactionToComment_withDifferentReactionTypes_shouldWork() throws Exception {
        // Test all reaction types
        ReactionType[] reactionTypes =
                new ReactionType[] {
                    ReactionType.HEART,
                    ReactionType.SMILEY,
                    ReactionType.SAD,
                    ReactionType.LAUGH,
                    ReactionType.ANGER
                };

        for (ReactionType reactionType : reactionTypes) {
            ReactionRequest request = new ReactionRequest(reactionType);

            doReturn(COMMENT_ID)
                    .when(commentService)
                    .addReactionToComment(USER_ID, COMMENT_ID, reactionType);

            mockMvc.perform(
                            post(COMMENTS_BASE_URL + "/{commentId}/reactions", COMMENT_ID)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isAccepted())
                    .andExpect(jsonPath("$").value(COMMENT_ID.toString()));
        }
    }

    @Test
    void addReactionToReply_shouldWorkSameAsTopLevelComment() throws Exception {
        // Given - Test that replies can have reactions too
        ReactionRequest request = new ReactionRequest(ReactionType.LAUGH);

        doReturn(COMMENT_ID)
                .when(commentService)
                .addReactionToComment(USER_ID, COMMENT_ID, ReactionType.LAUGH);

        // When & Then
        mockMvc.perform(
                        post(COMMENTS_BASE_URL + "/{commentId}/reactions", COMMENT_ID)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$").value(COMMENT_ID.toString()));

        verify(commentService).addReactionToComment(USER_ID, COMMENT_ID, ReactionType.LAUGH);
    }
}
