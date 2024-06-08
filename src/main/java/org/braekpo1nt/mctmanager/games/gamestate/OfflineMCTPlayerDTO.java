package org.braekpo1nt.mctmanager.games.gamestate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.braekpo1nt.mctmanager.config.validation.Validatable;
import org.braekpo1nt.mctmanager.config.validation.Validator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
    private String teamName;
    
    @Override
    public void validate(@NotNull Validator validator) {
        validator.notNull(ign, "ign");
        validator.notNull(teamName, "teamName");
    }
    
    OfflineMCTPlayer toOfflineMCTPlayer() {
        return new OfflineMCTPlayer(offlineUniqueId, ign, teamName);
    }
    
    static Map<String, OfflineMCTPlayer> toOfflineMCTPlayers(List<OfflineMCTPlayerDTO> offlineMCTPlayers) {
        return offlineMCTPlayers.stream().map(OfflineMCTPlayerDTO::toOfflineMCTPlayer).collect(Collectors.toMap(OfflineMCTPlayer::getIgn, Function.identity()));
    }
    
    static OfflineMCTPlayerDTO fromOfflineMCTPlayer(OfflineMCTPlayer offlineMCTPlayer) {
        return new OfflineMCTPlayerDTO(offlineMCTPlayer.getIgn(), offlineMCTPlayer.getOfflineUniqueId(), offlineMCTPlayer.getTeamName());
    }
    
    static List<OfflineMCTPlayerDTO> fromOfflineMCTPlayers(Map<String, OfflineMCTPlayer> offlineMCTPlayers) {
        return offlineMCTPlayers.values().stream().map(OfflineMCTPlayerDTO::fromOfflineMCTPlayer).toList();
    }
}
