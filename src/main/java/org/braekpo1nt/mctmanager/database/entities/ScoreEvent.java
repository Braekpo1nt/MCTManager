package org.braekpo1nt.mctmanager.database.entities;

import lombok.Builder;
import lombok.Data;
import org.braekpo1nt.mctmanager.games.gamemanager.Mode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Date;

/**
 * Represents a single score event, the change of a participant or team score.
 * Together, these historical journaled events are used to reconstruct the current
 * game state with respect to coins.
 */
@Builder
@Data
public class ScoreEvent {
    /**
     * what was the source of this score event? Most will be from {@link SourceType#GAME}
     */
    private @NotNull ScoreEvent.SourceType sourceType;
    /**
     * the id of the game session this score event took place during,
     * or null if not from a game
     */
    private @Nullable Integer gameSessionId;
    /**
     * the UUID of the participant this score was awarded to,
     * or null if this was not for a participant
     */
    private @Nullable String participantUUID;
    /**
     * the teamId of the team this score was awarded to,
     * or the teamId of the participant this score was awarded to if
     * {@link #participantUUID} is not null.
     */
    private @NotNull String teamId;
    /**
     * the base points that were awarded with this score event,
     * un-multiplied
     */
    private int pointsBase;
    /**
     * a description of the reason this score event happened (for human
     * readability)
     */
    private @NotNull String description;
    /**
     * the time stamp that this score event was created
     */
    private @NotNull Date createdAt;
    
    public @NotNull ScoreEventEntity toScoreEvent(
            @Nullable String eventId,
            @NotNull Mode mode
    ) {
        return ScoreEventEntity.builder()
                .sourceType(sourceType)
                .gameSessionId(gameSessionId)
                .eventId(eventId)
                .mode(mode)
                .participantUUID(participantUUID)
                .teamId(teamId)
                .pointsBase(pointsBase)
                .description(description)
                .createdAt(createdAt)
                .build();
    }
    
    public enum SourceType {
        GAME,
        ADMIN,
        SYSTEM,
        MIGRATION
    }
}
