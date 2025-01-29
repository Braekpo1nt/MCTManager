package org.braekpo1nt.mctmanager.games.gamestate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.braekpo1nt.mctmanager.config.validation.Validatable;
import org.braekpo1nt.mctmanager.config.validation.Validator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
class GameStateDTO implements Validatable {
    
    private @Nullable Map<UUID, MCTPlayerDTO> players = new HashMap<>();
    /**
     * Holds the list of players who are to be added upon joining
     */
    private @Nullable List<OfflineMCTPlayerDTO> offlinePlayers = new ArrayList<>();
    private @Nullable Map<String, MCTTeamDTO> teams = new HashMap<>();
    private @Nullable List<UUID> admins = new ArrayList<>();
    
    @Override
    public void validate(@NotNull Validator validator) {
        if (players == null && teams == null) {
            return;
        }
        validator.validate(players != null, "players can't be null if teams is null");
        validator.validate(teams != null, "teams can't be null if players is null");
        Set<UUID> uniqueUUIDs = new HashSet<>(players.size());
        for (Map.Entry<UUID, MCTPlayerDTO> entry : players.entrySet()) {
            MCTPlayerDTO mctPlayer = entry.getValue();
            validator.validate(mctPlayer != null, "players can't contain null values");
            mctPlayer.validate(validator.path("players[%s]", entry.getKey()));
            validator.validate(teams.containsKey(mctPlayer.getTeamId()), "players[%s].teamId could not be found in teams");
            UUID uuid = mctPlayer.getUniqueId();
            validator.validate(entry.getKey().equals(uuid), "players[%s].uniqueId must match it's key. Instead it was \"%s\"", entry.getKey(), uuid);
            validator.validate(!uniqueUUIDs.contains(uuid), "players[%s].uniqueId is a duplicate of \"%s\"", entry.getKey(), uuid);
            uniqueUUIDs.add(uuid);
        }
        if (offlinePlayers != null) {
            Set<String> uniqueIGNs = new HashSet<>(offlinePlayers.size());
            for (int i = 0; i < offlinePlayers.size(); i++) {
                OfflineMCTPlayerDTO offlineMCTPlayer = offlinePlayers.get(i);
                validator.validate(offlineMCTPlayer != null, "offlinePlayers can't contain null values");
                offlineMCTPlayer.validate(validator.path("offlinePlayers[%d]", i));
                String ign = offlineMCTPlayer.getIgn();
                validator.validate(!uniqueIGNs.contains(ign), "offlinePlayers[%d].ign is a duplicate of \"%s\"", i, ign);
                uniqueIGNs.add(ign);
            }
        }
        validator.validateMap(this.teams, "teams");
    }
    
    @NotNull GameState toGameState() {
        return GameState.builder()
                .players(this.players != null 
                        ? MCTPlayerDTO.toMCTPlayers(this.players) 
                        : new HashMap<>())
                .offlinePlayers(this.offlinePlayers != null 
                        ? OfflineMCTPlayerDTO.toOfflineMCTPlayers(this.offlinePlayers) 
                        : new HashMap<>())
                .teams(this.teams != null 
                        ? MCTTeamDTO.toTeams(this.teams) 
                        : new HashMap<>())
                .admins(this.admins != null 
                        ? this.admins 
                        : new ArrayList<>())
                .build();
    }
    
    static @NotNull GameStateDTO fromGameState(@NotNull GameState gameState) {
        return new GameStateDTO(
                MCTPlayerDTO.fromMCTPlayers(gameState.getPlayers()),
                OfflineMCTPlayerDTO.fromOfflineMCTPlayers(gameState.getOfflinePlayers()),
                MCTTeamDTO.fromTeams(gameState.getTeams()),
                gameState.getAdmins()
        );
    }
    
}
