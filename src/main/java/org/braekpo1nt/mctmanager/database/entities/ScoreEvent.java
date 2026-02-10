package org.braekpo1nt.mctmanager.database.entities;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Date;

/**
 * Represents a single score event, the change of a participant or team score.
 * Together, these historical journaled events are used to reconstruct the current
 * game state with respect to coins.
 */
@DatabaseTable(tableName = "score_events")
@NoArgsConstructor
@Data
public class ScoreEvent {
    @DatabaseField(generatedId = true)
    private int id;
    /**
     * what was the source of this score event? Most will be from {@link SourceType#GAME}
     */
    @DatabaseField(canBeNull = false, columnName = "source_type")
    private @NotNull SourceType sourceType;
    /**
     * the id of the game session this score event took place during,
     * or null if not from a game
     */
    @DatabaseField(columnName = "session_id")
    private @Nullable Integer gameSessionId;
    /**
     * The eventId of the event during which this score event took place,
     * or null if this was not during an event
     */
    @DatabaseField(columnName = "event_id")
    private @Nullable String eventId;
    /**
     * the UUID of the participant this score was awarded to,
     * or null if this was not for a participant
     */
    @DatabaseField(columnName = "participant_uuid")
    private @Nullable String participantUUID;
    /**
     * the teamId of the team this score was awarded to,
     * or the teamId of the participant this score was awarded to if
     * {@link #participantUUID} is not null.
     */
    @DatabaseField(columnName = "team_id")
    private @NotNull String teamId;
    /**
     * the base points that were awarded with this score event,
     * un-multiplied
     */
    @DatabaseField(canBeNull = false, columnName = "points_base")
    private int pointsBase;
    @DatabaseField(canBeNull = false, columnName = "multiplier")
    private double multiplier;
    /**
     * a description of the reason this score event happened (for human
     * readability)
     */
    @DatabaseField(canBeNull = false, columnName = "reason")
    private @NotNull String description;
    /**
     * the time stamp that this score event was created
     */
    @DatabaseField(canBeNull = false, columnName = "created_at")
    private @NotNull Date createdAt;
    
    public enum SourceType {
        GAME,
        ADMIN,
        SYSTEM,
        MIGRATION
    }
    
    @Builder
    public ScoreEvent(
            @NotNull SourceType sourceType,
            @Nullable Integer gameSessionId,
            @Nullable String eventId,
            @Nullable String participantUUID,
            @NotNull String teamId,
            int pointsBase,
            double multiplier,
            @NotNull String description,
            @NotNull Date createdAt
    ) {
        this.sourceType = sourceType;
        this.gameSessionId = gameSessionId;
        this.eventId = eventId;
        this.participantUUID = participantUUID;
        this.teamId = teamId;
        this.pointsBase = pointsBase;
        this.multiplier = multiplier;
        this.description = description;
        this.createdAt = createdAt;
    }
}
