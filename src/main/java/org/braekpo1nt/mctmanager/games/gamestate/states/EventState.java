package org.braekpo1nt.mctmanager.games.gamestate.states;

import org.braekpo1nt.mctmanager.database.entities.admin.ActiveAdminEntity;
import org.braekpo1nt.mctmanager.database.entities.admin.EventAdminEntity;
import org.braekpo1nt.mctmanager.database.entities.teams.ActiveTeam;
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
    
    /**
     * {@inheritDoc}
     */
    @Override
    public int addTeam(String teamId, String teamDisplayName, String color) throws SQLException {
        MCTTeamEntity team = context.getGameState().addTeam(teamId, teamDisplayName, color);
        context.getGameStateService().addTeam(team.toEvent(eventId));
        int score = context.getGameStateService().rebuildEventTeam(teamId, eventId, context.isMariaDB());
        team.setScore(score);
        return score;
    }
    
    @Override
    public void removeTeam(String teamId) throws SQLException {
        List<UUID> uuidsOnTeam = context.getParticipantUUIDsOnTeam(teamId);
        context.getGameState().removePlayers(uuidsOnTeam);
        context.getGameState().removeTeam(teamId);
        context.getGameStateService().deleteTeam(teamId);
        context.getGameStateService().deleteEventTeam(teamId, eventId);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public int addNewPlayer(@NotNull UUID uuid, @NotNull String ign, @NotNull String teamId) throws SQLException {
        MCTPlayerEntity player = context.getGameState().addPlayer(uuid, ign, teamId);
        context.getGameStateService().addParticipant(player.toEvent(eventId), player.getName());
        int score = context.getGameStateService().rebuildEventParticipant(uuid.toString(), eventId);
        player.setScore(score);
        return score;
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
