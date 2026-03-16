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
 * The teams used in maintenance mode
 */
@DatabaseTable(tableName = "maintenance_teams")
@NoArgsConstructor
@Data
@AllArgsConstructor
@Builder
public class MaintenanceTeam {
    @DatabaseField(id = true, columnName = "team_id")
    private @NotNull String teamId;
    @DatabaseField(canBeNull = false, columnName = "display_name")
    private @NotNull String displayName;
    @DatabaseField(canBeNull = false, columnName = "color")
    private @NotNull String color;
    @DatabaseField(canBeNull = false, columnName = "modified_at")
    private @NotNull Date modifiedAt;
}
