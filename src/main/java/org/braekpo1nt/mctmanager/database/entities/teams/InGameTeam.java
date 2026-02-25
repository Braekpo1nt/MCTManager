package org.braekpo1nt.mctmanager.database.entities.teams;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.braekpo1nt.mctmanager.participant.Team;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;

@DatabaseTable(tableName = "in_game_teams")
@NoArgsConstructor
@Data
@AllArgsConstructor
@Builder
public class InGameTeam {
    /**
     * The teamId of the team
     */
    @DatabaseField(id = true, columnName = "team_id")
    private @NotNull String teamId;
    /**
     * the id of the game session this team is participating in
     */
    @DatabaseField(columnName = "session_id")
    private @Nullable Integer gameSessionId;
    /**
     * The team's score in the game
     */
    @DatabaseField(canBeNull = false, columnName = "game_score")
    private int gameScore;
    
    public static <T extends Team> @NotNull InGameTeam from(@NotNull T team, int gameSessionId) {
        return InGameTeam.builder()
                .teamId(team.getTeamId())
                .gameSessionId(gameSessionId)
                .gameScore(team.getScore())
                .build();
    }
    
    public static <T extends Team> @NotNull List<InGameTeam> from(@NotNull Collection<T> teams, int gameSessionId) {
        return teams.stream()
                .map(team -> from(team, gameSessionId))
                .toList();
    }
}
