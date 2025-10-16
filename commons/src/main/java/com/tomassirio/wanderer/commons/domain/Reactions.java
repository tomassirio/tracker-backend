package com.tomassirio.wanderer.commons.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Reactions {
    @Builder.Default private Integer heart = 0;

    @Builder.Default private Integer smiley = 0;

    @Builder.Default private Integer sad = 0;

    @Builder.Default private Integer laugh = 0;

    @Builder.Default private Integer anger = 0;

    public void incrementReaction(ReactionType type) {
        switch (type) {
            case HEART -> this.heart++;
            case SMILEY -> this.smiley++;
            case SAD -> this.sad++;
            case LAUGH -> this.laugh++;
            case ANGER -> this.anger++;
        }
    }

    public void decrementReaction(ReactionType type) {
        switch (type) {
            case HEART -> this.heart = Math.max(0, this.heart - 1);
            case SMILEY -> this.smiley = Math.max(0, this.smiley - 1);
            case SAD -> this.sad = Math.max(0, this.sad - 1);
            case LAUGH -> this.laugh = Math.max(0, this.laugh - 1);
            case ANGER -> this.anger = Math.max(0, this.anger - 1);
        }
    }
}
