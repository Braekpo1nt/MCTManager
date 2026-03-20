package org.braekpo1nt.mctmanager.games.gamestate.states;

import org.braekpo1nt.mctmanager.games.gamestate.GameStateStorageUtil;
import org.braekpo1nt.mctmanager.games.gamestate.MCTPlayerEntity;
import org.braekpo1nt.mctmanager.games.gamestate.MCTTeamEntity;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

public class PracticeState extends StorageUtilState {
    
    public PracticeState(@NotNull GameStateStorageUtil context) {
        super(context);
    }
    
    @Override
    public void enter() {
        
    }
    
    @Override
    public void exit() {
        
    }
    
    @Override
    public void addTeam(String teamId, String teamDisplayName, String color) throws SQLException {
        MCTTeamEntity team = gameState.addTeam(teamId, teamDisplayName, color);
        gameStateService.addTeam(GameStateStorageUtil.fromTeam(team));
        gameStateService.addTeam(team.toPractice());
    }
    
    @Override
    public void removeTeam(String teamId) throws SQLException {
        List<UUID> uuidsOnTeam = context.getParticipantUUIDsOnTeam(teamId);
        gameState.removePlayers(uuidsOnTeam);
        gameState.removeTeam(teamId);
        gameStateService.deleteTeam(teamId);
        gameStateService.deletePracticeTeam(teamId);
    }
    
    @Override
    public void addNewPlayer(@NotNull UUID playerToJoin, @NotNull String name, @NotNull String teamId) throws SQLException {
        MCTPlayerEntity player = gameState.addPlayer(playerToJoin, name, teamId);
        gameStateService.addParticipant(GameStateStorageUtil.fromPlayer(player));
        gameStateService.addParticipant(player.toPractice(), player.getName());
    }
    
    @Override
    public void leavePlayer(UUID playerUniqueId) throws SQLException {
        gameState.removePlayer(playerUniqueId);
        String uuid = playerUniqueId.toString();
        gameStateService.deleteParticipant(uuid);
        gameStateService.deletePracticeParticipant(uuid);
    }
}
