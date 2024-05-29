package org.braekpo1nt.mctmanager.games.gamestate;

import lombok.Builder;
import lombok.Data;
import org.braekpo1nt.mctmanager.config.validation.Validatable;
import org.braekpo1nt.mctmanager.config.validation.Validator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

@Data
@Builder
class GameStateDTO implements Validatable {
    
    private @Nullable Map<UUID, MCTPlayerDTO> players;
    private @Nullable Map<String, MCTTeamDTO> teams;
    private @Nullable List<UUID> admins;
    
    @Override
    public void validate(Validator validator) {
        if (players == null && teams == null) {
            return;
        }
        validator.validate(players != null, "players can't be null if teams is null");
        validator.validate(teams != null, "teams can't be null if players is null");
        for (Map.Entry<UUID, MCTPlayerDTO> entry : players.entrySet()) {
            MCTPlayerDTO mctPlayer = entry.getValue();
            validator.validate(mctPlayer != null, "players can't contain null values");
            mctPlayer.validate(validator.path("players[%s]", entry.getKey()));
            validator.validate(teams.containsKey(mctPlayer.getTeamName()), "players[%s].teamName could not be found in teams");
        }
        for (Map.Entry<String, MCTTeamDTO> entry : teams.entrySet()) {
            MCTTeamDTO mctTeam = entry.getValue();
            validator.validate(mctTeam != null, "teams can't contain null values");
            mctTeam.validate(validator.path("teams[%s]", entry.getKey()));
        }
    }
    
    @NotNull GameState toGameState() {
        return GameState.builder()
                .players(this.players != null ? MCTPlayerDTO.toMCTPlayers(this.players) : new HashMap<>())
                .teams(this.teams != null ? MCTTeamDTO.toMCTTeams(this.teams) : new HashMap<>())
                .admins(this.admins != null ? this.admins : new ArrayList<>())
                .build();
    }
    
    static @NotNull GameStateDTO fromGameState(@NotNull GameState gameState) {
        return GameStateDTO.builder()
                .players(MCTPlayerDTO.fromMCTPlayers(gameState.getPlayers()))
                .teams(MCTTeamDTO.fromMCTTeams(gameState.getTeams()))
                .admins(gameState.getAdmins())
                .build();
    }
    
}
