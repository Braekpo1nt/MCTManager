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
    /**
     * the eventId associated with this GameSession, or null if it was
     * not during an event
     */
    @DatabaseField(columnName = "event_id")
    private @Nullable String eventId;
    @DatabaseField(canBeNull = false, columnName = "game_type")
    private @NotNull GameType gameType;
    @DatabaseField(canBeNull = false, columnName = "config_file")
    private @NotNull String configFile;
    /**
     * what mode were we in when this session took place
     */
    @DatabaseField(canBeNull = false, columnName = "mode")
    private @NotNull String mode;
    @DatabaseField(canBeNull = false, columnName = "start_time")
    private @NotNull Date startTime;
    @DatabaseField(columnName = "end_time")
    private @Nullable Date endTime;
    
    @Builder
    public GameSession(
            @NotNull GameType gameType,
            @Nullable String eventId,
            @NotNull String configFile,
            @NotNull String mode,
            @NotNull Date startTime
    ) {
        this.gameType = gameType;
        this.eventId = eventId;
        this.configFile = configFile;
        this.mode = mode;
        this.startTime = startTime;
    }
}
