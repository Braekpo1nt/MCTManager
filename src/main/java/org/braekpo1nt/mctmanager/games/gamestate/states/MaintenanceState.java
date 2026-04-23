package org.braekpo1nt.mctmanager.games.gamestate.states;

import org.braekpo1nt.mctmanager.config.exceptions.ConfigIOException;
import org.braekpo1nt.mctmanager.database.entities.admin.ActiveAdminEntity;
import org.braekpo1nt.mctmanager.database.entities.admin.MaintenanceAdminEntity;
import org.braekpo1nt.mctmanager.games.gamestate.GameStateStorageUtil;
import org.braekpo1nt.mctmanager.games.gamestate.MCTPlayerEntity;
import org.braekpo1nt.mctmanager.games.gamestate.MCTTeamEntity;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

public class MaintenanceState extends StorageUtilState {
    
    public MaintenanceState(@NotNull GameStateStorageUtil context) {
        super(context);
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
        context.getGameStateService().addTeam(team.toMaintenance());
        int score = context.getGameStateService().rebuildMaintenanceTeam(teamId, context.isMariaDB());
        team.setScore(score);
        return score;
    }
    
    @Override
    public void removeTeam(String teamId) throws SQLException {
        List<UUID> uuidsOnTeam = context.getParticipantUUIDsOnTeam(teamId);
        context.getGameState().removePlayers(uuidsOnTeam);
        context.getGameState().removeTeam(teamId);
        context.getGameStateService().deleteTeam(teamId);
        context.getGameStateService().deleteMaintenanceTeam(teamId);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public int addNewPlayer(@NotNull UUID uuid, @NotNull String ign, @NotNull String teamId) throws ConfigIOException, SQLException {
        MCTPlayerEntity player = context.getGameState().addPlayer(uuid, ign, teamId);
        context.getGameStateService().addParticipant(player.toMaintenance(), player.getName());
        int score = context.getGameStateService().rebuildMaintenanceParticipant(uuid.toString());
        player.setScore(score);
        return score;
        // TODO: write a test to make sure the ActiveParticipant row is added to the active_participants table every time, and you don't have to add the below line. Do so for all states
//        context.getGameStateService().addParticipant(ActiveParticipant.fromPlayer(player));
    }
    
    @Override
    public void leavePlayer(UUID playerUniqueId) throws SQLException {
        context.getGameState().removePlayer(playerUniqueId);
        String uuid = playerUniqueId.toString();
        context.getGameStateService().deleteParticipant(uuid);
        context.getGameStateService().deleteMaintenanceParticipant(uuid);
    }
    
    @Override
    public void addAdmin(UUID adminUniqueId) throws SQLException {
        context.getGameState().addAdmin(adminUniqueId);
        String uuid = adminUniqueId.toString();
        context.getGameStateService().addAdmin(new ActiveAdminEntity(uuid));
        context.getGameStateService().addAdmin(new MaintenanceAdminEntity(uuid));
    }
    
    @Override
    public void removeAdmin(UUID adminUniqueId) throws SQLException {
        context.getGameState().removeAdmin(adminUniqueId);
        String uuid = adminUniqueId.toString();
        context.getGameStateService().deleteActiveAdmin(uuid);
        context.getGameStateService().deleteMaintenanceAdmin(uuid);
    }
}
