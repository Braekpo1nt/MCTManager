package org.braekpo1nt.mctmanager.database.entities.participants;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

@DatabaseTable(tableName = "active_participants")
@NoArgsConstructor
@Data
public class ActiveParticipant {
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
    /**
     * The Minecraft IGN
     */
    @DatabaseField(canBeNull = false, columnName = "ign")
    private @NotNull String ign;
    /**
     * The participant's score
     */
    @DatabaseField(canBeNull = false, columnName = "score")
    private int score;
}
