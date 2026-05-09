package org.braekpo1nt.mctmanager.database.entities.teams;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.braekpo1nt.mctmanager.games.gamestate.MCTTeamEntity;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

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
    
    public static Map<String, MCTTeamEntity> toTeams(List<ActiveTeam> activeTeams) {
        return activeTeams.stream()
                .map(ActiveTeam::toTeam)
                .collect(Collectors.toMap(MCTTeamEntity::getName, Function.identity()));
    }
    
    public static MCTTeamEntity toTeam(ActiveTeam activeTeam) {
        return MCTTeamEntity.builder()
                .name(activeTeam.getTeamId())
                .displayName(activeTeam.getDisplayName())
                .score(activeTeam.getScore())
                .color(activeTeam.getColor())
                .build();
    }
    
    public static List<ActiveTeam> fromTeams(Collection<MCTTeamEntity> entities) {
        return entities.stream()
                .map(ActiveTeam::fromTeam)
                .toList();
    }
    
    @Contract("null -> null")
    public static ActiveTeam fromTeam(MCTTeamEntity team) {
        if (team == null) {
            return null;
        }
        return builder()
                .teamId(team.getName())
                .displayName(team.getDisplayName())
                .color(team.getColor())
                .score(team.getScore())
                .build();
    }
}
