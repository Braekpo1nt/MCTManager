package org.braekpo1nt.mctmanager.games.gamestate;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.braekpo1nt.mctmanager.config.validation.Validatable;
import org.braekpo1nt.mctmanager.config.validation.Validator;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Data
@AllArgsConstructor
class MCTPlayerDTO implements Validatable {
    private UUID uniqueId;
    private int score;
    private String teamName;
    
    @Override
    public void validate(@NotNull Validator validator) {
        validator.notNull(uniqueId, "uniqueId");
        validator.validate(score >= 0, "score can't be negative");
        validator.notNull(teamName, "teamName");
    }
    
    public static Map<UUID, MCTPlayerDTO> fromMCTPlayers(Map<UUID, MCTPlayer> players) {
        Map<UUID, MCTPlayerDTO> playerDTOs = new HashMap<>();
        for (Map.Entry<UUID, MCTPlayer> entry : players.entrySet()) {
            playerDTOs.put(entry.getKey(), MCTPlayerDTO.fromMCTPlayer(entry.getValue()));
        }
        return playerDTOs;
    }
    
    static MCTPlayerDTO fromMCTPlayer(MCTPlayer mctPlayer) {
        return new MCTPlayerDTO(
                mctPlayer.getUniqueId(),
                mctPlayer.getScore(),
                mctPlayer.getTeamName());
    }
    
    MCTPlayer toMCTPlayer() {
        return MCTPlayer.builder()
                .uniqueId(this.uniqueId)
                .score(this.score)
                .teamName(this.teamName)
                .build();
    }
    
    public static Map<UUID, MCTPlayer> toMCTPlayers(Map<UUID, MCTPlayerDTO> playerDTOs) {
        Map<UUID, MCTPlayer> players = new HashMap<>();
        for (Map.Entry<UUID, MCTPlayerDTO> entry : playerDTOs.entrySet()) {
            players.put(entry.getKey(), entry.getValue().toMCTPlayer());
        }
        return players;
    }
}
