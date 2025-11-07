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
 * An instantaneous personal score, earned by a participant.
 * For example, for a kill in a survival games, or reaching a checkpoint in parkour pathway.
 */
@DatabaseTable(tableName = "instant_personal_scores")
@NoArgsConstructor
@Data
public class InstantPersonalScore {
    @DatabaseField(generatedId = true)
    private int id;
    @DatabaseField(canBeNull = false)
    private @NotNull String uuid;
    @DatabaseField(canBeNull = false)
    private @NotNull String teamId;
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
    @DatabaseField(canBeNull = false)
    private @NotNull String description;
    
    @Builder
    public InstantPersonalScore(
            @NotNull String uuid,
            @NotNull String teamId,
            @NotNull GameType gameType,
            @NotNull String configFile,
            @NotNull Date date,
            @NotNull String mode,
            double multiplier,
            int points,
            @NotNull String description
    ) {
        this.uuid = uuid;
        this.teamId = teamId;
        this.gameType = gameType;
        this.configFile = configFile;
        this.date = date;
        this.mode = mode;
        this.multiplier = multiplier;
        this.points = points;
        this.description = description;
    }
}
