package org.braekpo1nt.mctmanager.database.entities;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.braekpo1nt.mctmanager.games.game.enums.GameType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Date;

@DatabaseTable(tableName = "game_sessions")
@NoArgsConstructor
@Data
public class GameSession {
    @DatabaseField(generatedId = true)
    private int id;
    @DatabaseField(canBeNull = false)
    private @NotNull GameType gameType;
    @DatabaseField(canBeNull = false)
    private @NotNull String configFile;
    @DatabaseField(canBeNull = false)
    private @NotNull Date startTime;
    @DatabaseField
    private @Nullable Date endTime;
    @DatabaseField(canBeNull = false)
    private @NotNull String mode;
    
    @Builder
    public GameSession(
            @NotNull GameType gameType,
            @NotNull String configFile,
            @NotNull Date startTime,
            @NotNull String mode
    ) {
        this.gameType = gameType;
        this.configFile = configFile;
        this.startTime = startTime;
        this.mode = mode;
    }
}
