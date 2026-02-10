package org.braekpo1nt.mctmanager.database.entities.participants;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

/**
 * Who is a participant in maintenance mode
 */
@DatabaseTable(tableName = "event_participants")
@NoArgsConstructor
@Data
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
     * The UUID of the participant
     */
    @DatabaseField(id = true, columnName = "participant_uuid")
    private @NotNull String participantUUID;
}
