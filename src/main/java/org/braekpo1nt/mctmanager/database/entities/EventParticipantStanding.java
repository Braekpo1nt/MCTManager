package org.braekpo1nt.mctmanager.database.entities;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

/**
 * A projection table, reset on plugin load, used for swift reference
 * of the current participant standings during the active event
 */
@DatabaseTable(tableName = "event_participant_standings")
@NoArgsConstructor
@Data
public class EventParticipantStanding {
    @DatabaseField(generatedId = true)
    private int id;
    /**
     * the eventId of the event this participant is associated with.
     * Unique in combination with {@link #participantUUID}
     */
    @DatabaseField(canBeNull = false, columnName = "event_id")
    private @NotNull String eventId;
    /**
     * The UUID of the participant.
     * Unique in combination with {@link #eventId}
     */
    @DatabaseField(canBeNull = false, columnName = "participant_uuid")
    private @NotNull String participantUUID;
    /**
     * The current score of the participant
     */
    @DatabaseField(canBeNull = false, columnName = "score")
    private int score;
}
