package org.braekpo1nt.mctmanager.database.entities.teams;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.Date;

/**
 * The teams associated with each event
 */
@DatabaseTable(tableName = "event_teams")
@NoArgsConstructor
@Data
@AllArgsConstructor
@Builder
public class EventTeam {
    /**
     * Generated unique id, for uniqueness only
     */
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
    private @NotNull String displayName;
    @DatabaseField(canBeNull = false, columnName = "color")
    private @NotNull String color;
    @DatabaseField(canBeNull = false, columnName = "modified_at")
    private @NotNull Date modifiedAt;
}
