package org.braekpo1nt.mctmanager.database.entities.participants;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

/**
 * Who is a participant in practice mode
 */
@DatabaseTable(tableName = "practice_participants")
@NoArgsConstructor
@Data
public class PracticeParticipantEntity {
    /**
     * The UUID of the participant
     */
    @DatabaseField(id = true, columnName = "participant_uuid")
    private @NotNull String participantUUID;
    /**
     * The teamId this participant is a member of
     */
    @DatabaseField(canBeNull = false, columnName = "team_id")
    private @NotNull String teamId;
}
