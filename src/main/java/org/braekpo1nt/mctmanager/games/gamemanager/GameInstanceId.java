package org.braekpo1nt.mctmanager.games.gamemanager;

import lombok.Data;
import org.braekpo1nt.mctmanager.games.game.enums.GameType;
import org.jetbrains.annotations.NotNull;

/**
 * Used to identify a specific game instance, made distinct by which game type
 * and which config file is being used by the instance.
 */
@Data
public class GameInstanceId {
    private final @NotNull GameType gameType;
    private final @NotNull String configFile;
    
    public String getTitle() {
        return gameType.getTitle();
    }
}
