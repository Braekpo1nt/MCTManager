package org.braekpo1nt.mctmanager.games.gamestate.states;

import org.braekpo1nt.mctmanager.database.entities.admin.EventAdminEntity;
import org.braekpo1nt.mctmanager.games.gamestate.GameStateStorageUtil;
import org.braekpo1nt.mctmanager.games.gamestate.MCTPlayerEntity;
import org.braekpo1nt.mctmanager.games.gamestate.MCTTeamEntity;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;

public class SUEventState extends StorageUtilState {
    
    private final @NotNull String eventId;
    
    public SUEventState(@NotNull GameStateStorageUtil context, @NotNull String eventId) {
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
    protected int persistAddTeam(String teamId, MCTTeamEntity team) throws SQLException {
        context.getGameStateService().addTeam(team.toEvent(eventId));
        return context.getGameStateService().rebuildEventTeam(teamId, eventId, context.isMariaDB());
    }
    
    @Override
    protected void persistRemoveTeam(String teamId) throws SQLException {
        context.getGameStateService().deleteEventTeam(teamId, eventId);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected int persistJoinPlayer(@NotNull UUID uuid, MCTPlayerEntity player) throws SQLException {
        context.getGameStateService().addParticipant(player.toEvent(eventId), player.getName());
        return context.getGameStateService().rebuildEventParticipant(uuid.toString(), eventId);
    }
    
    @Override
    public void persistLeavePlayer(@NotNull String uuidString) throws SQLException {
        context.getGameStateService().deleteEventParticipant(uuidString, eventId);
    }
    
    @Override
    public void persistAddAdmin(@NotNull String uuid) throws SQLException {
        context.getGameStateService().addAdmin(EventAdminEntity.builder()
                .eventId(eventId)
                .uuid(uuid)
                .build());
    }
    
    @Override
    public void persistRemoveAdmin(@NotNull String uuid) throws SQLException {
        context.getGameStateService().deleteEventAdmin(uuid, eventId);
    }
    
    @Override
    protected @NotNull Map<UUID, String> getAdminNames() throws SQLException {
        return context.getGameStateService().getEventAdminNames(eventId);
    }
}
