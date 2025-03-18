package org.braekpo1nt.mctmanager.games.gamestate;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.braekpo1nt.mctmanager.config.validation.Validatable;
import org.braekpo1nt.mctmanager.config.validation.Validator;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
class MCTPlayerDTO implements Validatable {
    private UUID uniqueId;
    private String name;
    private int score;
    @SerializedName(value = "teamId", alternate = {"teamName"})
    private String teamId;
    
    @Override
    public void validate(@NotNull Validator validator) {
        validator.notNull(uniqueId, "uniqueId");
        validator.notNull(name, "name");
        validator.validate(score >= 0, "score can't be negative");
        validator.notNull(teamId, "teamId");
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
                mctPlayer.getName(),
                mctPlayer.getScore(),
                mctPlayer.getTeamId());
    }
    
    MCTPlayer toMCTPlayer() {
        return MCTPlayer.builder()
                .uniqueId(this.uniqueId)
                .name(this.name)
                .score(this.score)
                .teamId(this.teamId)
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
