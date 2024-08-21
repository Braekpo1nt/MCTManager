package org.braekpo1nt.mctmanager.games.gamestate;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * Represents a participant who has never logged on before
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class OfflineMCTPlayer {
    private @Nullable UUID offlineUniqueId;
    /**
     * the in-game-name of the participant
     */
    private String ign;
    @SerializedName(value = "teamId", alternate = {"teamName"})
    private String teamId;
}
