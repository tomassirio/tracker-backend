package com.tomassirio.wanderer.commons.domain;

import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;

@Entity
@Table(name = "comments")
@Data
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotNull
    @Column(name = "user_id", nullable = false)
    private UUID userId; // Author of the comment

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trip_id", nullable = false)
    private Trip trip;

    // Self-referential relationship: a comment can be a reply to another comment
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_comment_id")
    private Comment parentComment;

    @OneToMany(mappedBy = "parentComment", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Comment> replies;

    @NotBlank
    @Column(name = "message", nullable = false, columnDefinition = "TEXT")
    private String message;

    @Type(JsonBinaryType.class)
    @Column(name = "reactions", columnDefinition = "jsonb")
    private Reactions reactions;

    @NotNull
    @Column(nullable = false)
    private Instant timestamp;

    /**
     * Checks if this comment is a top-level comment (not a reply).
     *
     * @return true if this is a top-level comment, false if it's a reply
     */
    public boolean isTopLevel() {
        return parentComment == null;
    }

    /**
     * Checks if this comment is a reply to another comment.
     *
     * @return true if this is a reply, false if it's a top-level comment
     */
    public boolean isReply() {
        return parentComment != null;
    }
}
