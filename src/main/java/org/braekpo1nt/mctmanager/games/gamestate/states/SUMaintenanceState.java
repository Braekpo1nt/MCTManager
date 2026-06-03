package org.braekpo1nt.mctmanager.games.gamestate.states;

import org.braekpo1nt.mctmanager.database.entities.admin.MaintenanceAdminEntity;
import org.braekpo1nt.mctmanager.games.gamestate.GameStateStorageUtil;
import org.braekpo1nt.mctmanager.games.gamestate.MCTPlayerEntity;
import org.braekpo1nt.mctmanager.games.gamestate.MCTTeamEntity;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;

public class SUMaintenanceState extends StorageUtilState {
    
    public SUMaintenanceState(@NotNull GameStateStorageUtil context) {
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
    protected int persistAddTeam(String teamId, MCTTeamEntity team) throws SQLException {
        context.getGameStateService().addTeam(team.toMaintenance());
        return context.getGameStateService().rebuildMaintenanceTeam(teamId, context.isMariaDB());
    }
    
    @Override
    protected void persistRemoveTeam(String teamId) throws SQLException {
        context.getGameStateService().deleteMaintenanceTeam(teamId);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected int persistJoinPlayer(@NotNull UUID uuid, MCTPlayerEntity player) throws SQLException {
        context.getGameStateService().addParticipant(player.toMaintenance(), player.getName());
        return context.getGameStateService().rebuildMaintenanceParticipant(uuid.toString());
    }
    
    @Override
    public void persistLeavePlayer(@NotNull String uuidString) throws SQLException {
        context.getGameStateService().deleteMaintenanceParticipant(uuidString);
    }
    
    @Override
    public void persistAddAdmin(@NotNull String uuid) throws SQLException {
        context.getGameStateService().addAdmin(new MaintenanceAdminEntity(uuid));
    }
    
    @Override
    public void persistRemoveAdmin(@NotNull String uuid) throws SQLException {
        context.getGameStateService().deleteMaintenanceAdmin(uuid);
    }
    
    @Override
    protected @NotNull Map<UUID, String> getAdminNames() throws SQLException {
        return context.getGameStateService().getMaintenanceAdminNames();
    }
}
