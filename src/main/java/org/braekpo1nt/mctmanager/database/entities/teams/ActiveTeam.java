package org.braekpo1nt.mctmanager.database.entities.teams;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

@DatabaseTable(tableName = "active_teams")
@NoArgsConstructor
@Data
@AllArgsConstructor
@Builder
public class ActiveTeam {
    /**
     * The teamId of the team
     */
    @DatabaseField(id = true, columnName = "team_id")
    private @NotNull String teamId;
    /**
     * The team's display name
     */
    @DatabaseField(canBeNull = false, columnName = "display_name")
    private @NotNull String displayName;
    /**
     * The team's color
     */
    @DatabaseField(canBeNull = false, columnName = "color")
    private @NotNull String color;
    /**
     * The team's score
     */
    @DatabaseField(canBeNull = false, columnName = "score")
    private int score;
}
