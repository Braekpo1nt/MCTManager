package org.braekpo1nt.mctmanager.database.entities;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.braekpo1nt.mctmanager.games.game.enums.GameType;
import org.jetbrains.annotations.NotNull;

import java.util.Date;

/**
 * Represents a player's personal score at the end of a game
 */
@DatabaseTable(tableName = "final_personal_scores")
@NoArgsConstructor
@Data
public class FinalPersonalScore {
    @DatabaseField(generatedId = true)
    private int id;
    @DatabaseField(canBeNull = false)
    private @NotNull String uuid;
    @DatabaseField(canBeNull = false)
    private @NotNull String ign;
    @DatabaseField(canBeNull = false)
    private @NotNull String teamId;
    @DatabaseField(canBeNull = false)
    private int gameSessionId;
    @DatabaseField(canBeNull = false)
    private @NotNull GameType gameType;
    @DatabaseField(canBeNull = false)
    private @NotNull String configFile;
    @DatabaseField(canBeNull = false)
    private @NotNull Date date;
    @DatabaseField(canBeNull = false)
    private @NotNull String mode;
    @DatabaseField(defaultValue = "1.0")
    private double multiplier;
    @DatabaseField(defaultValue = "0")
    private int points;
    
    @Builder
    public FinalPersonalScore(
            @NotNull String uuid,
            @NotNull String ign,
            @NotNull String teamId,
            int gameSessionId,
            @NotNull GameType gameType,
            @NotNull String configFile,
            @NotNull Date date,
            @NotNull String mode,
            double multiplier,
            int points
    ) {
        this.uuid = uuid;
        this.ign = ign;
        this.teamId = teamId;
        this.gameSessionId = gameSessionId;
        this.gameType = gameType;
        this.configFile = configFile;
        this.date = date;
        this.mode = mode;
        this.multiplier = multiplier;
        this.points = points;
    }
}
