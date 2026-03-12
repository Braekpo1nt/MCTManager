package org.braekpo1nt.mctmanager.database.entities.participants;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

/**
 * Who is a participant in maintenance mode
 */
@DatabaseTable(tableName = "event_participants")
@NoArgsConstructor
@Data
@AllArgsConstructor
@Builder
public class EventParticipantEntity {
    /**
     * Generated unique id, for uniqueness only
     */
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
     * The teamId of the participant
     */
    @DatabaseField(canBeNull = false, columnName = "team_id")
    private @NotNull String teamId;
}
