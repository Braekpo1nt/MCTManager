package org.braekpo1nt.mctmanager.games.gamestate;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.braekpo1nt.mctmanager.config.validation.Validatable;
import org.braekpo1nt.mctmanager.config.validation.Validator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Represents a player who has never logged on before
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
class OfflineMCTPlayerDTO implements Validatable {
    /**
     * The in-game-name of the offline participant
     */
    private String ign;
    private @Nullable UUID offlineUniqueId;
    @SerializedName(value = "teamId", alternate = {"teamName"})
    private String teamId;
    
    @Override
    public void validate(@NotNull Validator validator) {
        validator.notNull(ign, "ign");
        validator.notNull(teamId, "teamId");
    }
    
    OfflineMCTPlayer toOfflineMCTPlayer() {
        return new OfflineMCTPlayer(offlineUniqueId, ign, teamId);
    }
    
    static Map<String, OfflineMCTPlayer> toOfflineMCTPlayers(List<OfflineMCTPlayerDTO> offlineMCTPlayers) {
        return offlineMCTPlayers.stream().map(OfflineMCTPlayerDTO::toOfflineMCTPlayer).collect(Collectors.toMap(OfflineMCTPlayer::getIgn, Function.identity()));
    }
    
    static OfflineMCTPlayerDTO fromOfflineMCTPlayer(OfflineMCTPlayer offlineMCTPlayer) {
        return new OfflineMCTPlayerDTO(offlineMCTPlayer.getIgn(), offlineMCTPlayer.getOfflineUniqueId(), offlineMCTPlayer.getTeamId());
    }
    
    static List<OfflineMCTPlayerDTO> fromOfflineMCTPlayers(Map<String, OfflineMCTPlayer> offlineMCTPlayers) {
        if (offlineMCTPlayers.isEmpty()) {
            return Collections.emptyList();
        }
        return offlineMCTPlayers.values().stream().map(OfflineMCTPlayerDTO::fromOfflineMCTPlayer).toList();
    }
}
