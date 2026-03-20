package org.braekpo1nt.mctmanager.games.gamestate.states;

import org.braekpo1nt.mctmanager.games.gamestate.GameStateStorageUtil;
import org.braekpo1nt.mctmanager.games.gamestate.MCTPlayerEntity;
import org.braekpo1nt.mctmanager.games.gamestate.MCTTeamEntity;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

public class EventState extends StorageUtilState {
    
    private final @NotNull String eventId;
    
    public EventState(@NotNull GameStateStorageUtil context, @NotNull String eventId) {
        super(context);
        this.eventId = eventId;
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
        gameStateService.addTeam(team.toEvent(eventId));
    }
    
    @Override
    public void removeTeam(String teamId) throws SQLException {
        List<UUID> uuidsOnTeam = context.getParticipantUUIDsOnTeam(teamId);
        gameState.removePlayers(uuidsOnTeam);
        gameState.removeTeam(teamId);
        gameStateService.deleteTeam(teamId);
        gameStateService.deleteEventTeam(teamId, eventId);
    }
    
    @Override
    public void addNewPlayer(@NotNull UUID playerToJoin, @NotNull String name, @NotNull String teamId) throws SQLException {
        MCTPlayerEntity player = gameState.addPlayer(playerToJoin, name, teamId);
        gameStateService.addParticipant(GameStateStorageUtil.fromPlayer(player));
        gameStateService.addParticipant(player.toEvent(eventId), player.getName());
    }
    
    @Override
    public void leavePlayer(UUID playerUniqueId) throws SQLException {
        gameState.removePlayer(playerUniqueId);
        String uuid = playerUniqueId.toString();
        gameStateService.deleteParticipant(uuid);
        gameStateService.deleteEventParticipant(uuid, eventId);
    }
}
