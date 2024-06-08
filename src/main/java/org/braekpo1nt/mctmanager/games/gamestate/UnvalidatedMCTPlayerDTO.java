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
class UnvalidatedMCTPlayerDTO implements Validatable {
    /**
     * The in-game-name of the unvalidated participant
     */
    private String ign;
    private @Nullable UUID offlineUniqueId;
    private String teamName;
    
    @Override
    public void validate(@NotNull Validator validator) {
        validator.notNull(ign, "ign");
        validator.notNull(teamName, "teamName");
    }
    
    UnvalidatedMCTPlayer toUnvalidatedMCTPlayer() {
        return new UnvalidatedMCTPlayer(offlineUniqueId, ign, teamName);
    }
    
    static Map<String, UnvalidatedMCTPlayer> toUnvalidatedMCTPlayers(List<UnvalidatedMCTPlayerDTO> uvPlayers) {
        return uvPlayers.stream().map(UnvalidatedMCTPlayerDTO::toUnvalidatedMCTPlayer).collect(Collectors.toMap(UnvalidatedMCTPlayer::getIgn, Function.identity()));
    }
    
    static UnvalidatedMCTPlayerDTO fromUnvalidatedMCTPlayer(UnvalidatedMCTPlayer uvPlayer) {
        return new UnvalidatedMCTPlayerDTO(uvPlayer.getIgn(), uvPlayer.getOfflineUniqueId(), uvPlayer.getTeamName());
    }
    
    static List<UnvalidatedMCTPlayerDTO> fromUnvalidatedMCTPlayers(Map<String, UnvalidatedMCTPlayer> uvPlayers) {
        return uvPlayers.values().stream().map(UnvalidatedMCTPlayerDTO::fromUnvalidatedMCTPlayer).toList();
    }
}
