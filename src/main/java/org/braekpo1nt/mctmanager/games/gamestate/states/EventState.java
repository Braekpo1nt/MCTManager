package org.braekpo1nt.mctmanager.games.gamestate.states;

import org.braekpo1nt.mctmanager.database.entities.admin.ActiveAdminEntity;
import org.braekpo1nt.mctmanager.database.entities.admin.EventAdminEntity;
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
        MCTTeamEntity team = context.getGameState().addTeam(teamId, teamDisplayName, color);
        context.getGameStateService().addTeam(GameStateStorageUtil.fromTeam(team));
        context.getGameStateService().addTeam(team.toEvent(eventId));
    }
    
    @Override
    public void removeTeam(String teamId) throws SQLException {
        List<UUID> uuidsOnTeam = context.getParticipantUUIDsOnTeam(teamId);
        context.getGameState().removePlayers(uuidsOnTeam);
        context.getGameState().removeTeam(teamId);
        context.getGameStateService().deleteTeam(teamId);
        context.getGameStateService().deleteEventTeam(teamId, eventId);
    }
    
    @Override
    public void addNewPlayer(@NotNull UUID playerToJoin, @NotNull String name, @NotNull String teamId) throws SQLException {
        MCTPlayerEntity player = context.getGameState().addPlayer(playerToJoin, name, teamId);
        context.getGameStateService().addParticipant(GameStateStorageUtil.fromPlayer(player));
        context.getGameStateService().addParticipant(player.toEvent(eventId), player.getName());
    }
    
    @Override
    public void leavePlayer(UUID playerUniqueId) throws SQLException {
        context.getGameState().removePlayer(playerUniqueId);
        String uuid = playerUniqueId.toString();
        context.getGameStateService().deleteParticipant(uuid);
        context.getGameStateService().deleteEventParticipant(uuid, eventId);
    }
    
    @Override
    public void addAdmin(UUID adminUniqueId) throws SQLException {
        context.getGameState().addAdmin(adminUniqueId);
        String uuid = adminUniqueId.toString();
        context.getGameStateService().addAdmin(new ActiveAdminEntity(uuid));
        context.getGameStateService().addAdmin(EventAdminEntity.builder()
                .eventId(eventId)
                .uuid(uuid)
                .build());
    }
    
    @Override
    public void removeAdmin(UUID adminUniqueId) throws SQLException {
        context.getGameState().removeAdmin(adminUniqueId);
        String uuid = adminUniqueId.toString();
        context.getGameStateService().deleteActiveAdmin(uuid);
        context.getGameStateService().deleteEventAdmin(uuid, eventId);
    }
}
