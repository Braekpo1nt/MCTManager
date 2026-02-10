package org.braekpo1nt.mctmanager.database.entities;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

/**
 * A projection table, reset on plugin load, used for swift reference
 * of the current team standings during the active event
 */
@DatabaseTable(tableName = "event_team_standings")
@NoArgsConstructor
@Data
public class EventTeamStanding {
    @DatabaseField(generatedId = true)
    private int id;
    /**
     * the eventId of the event this team is associated with.
     * Unique in combination with {@link #teamId}
     */
    @DatabaseField(canBeNull = false, columnName = "event_id")
    private @NotNull String eventId;
    /**
     * The teamId of this team.
     * Unique in combination with {@link #eventId}
     */
    @DatabaseField(canBeNull = false, columnName = "team_id")
    private @NotNull String teamId;
    /**
     * The current score of the team
     */
    @DatabaseField(canBeNull = false, columnName = "score")
    private int score;
}
