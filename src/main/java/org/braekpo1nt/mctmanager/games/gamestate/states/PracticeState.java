package org.braekpo1nt.mctmanager.games.gamestate.states;

import org.braekpo1nt.mctmanager.database.entities.admin.ActiveAdminEntity;
import org.braekpo1nt.mctmanager.database.entities.admin.PracticeAdminEntity;
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
        MCTTeamEntity team = context.getGameState().addTeam(teamId, teamDisplayName, color);
        context.getGameStateService().addTeam(GameStateStorageUtil.fromTeam(team));
        context.getGameStateService().addTeam(team.toPractice());
    }
    
    @Override
    public void removeTeam(String teamId) throws SQLException {
        List<UUID> uuidsOnTeam = context.getParticipantUUIDsOnTeam(teamId);
        context.getGameState().removePlayers(uuidsOnTeam);
        context.getGameState().removeTeam(teamId);
        context.getGameStateService().deleteTeam(teamId);
        context.getGameStateService().deletePracticeTeam(teamId);
    }
    
    @Override
    public void addNewPlayer(@NotNull UUID playerToJoin, @NotNull String name, @NotNull String teamId) throws SQLException {
        MCTPlayerEntity player = context.getGameState().addPlayer(playerToJoin, name, teamId);
        context.getGameStateService().addParticipant(GameStateStorageUtil.fromPlayer(player));
        context.getGameStateService().addParticipant(player.toPractice(), player.getName());
    }
    
    @Override
    public void leavePlayer(UUID playerUniqueId) throws SQLException {
        context.getGameState().removePlayer(playerUniqueId);
        String uuid = playerUniqueId.toString();
        context.getGameStateService().deleteParticipant(uuid);
        context.getGameStateService().deletePracticeParticipant(uuid);
    }
    
    @Override
    public void addAdmin(UUID adminUniqueId) throws SQLException {
        context.getGameState().addAdmin(adminUniqueId);
        String uuid = adminUniqueId.toString();
        context.getGameStateService().addAdmin(new ActiveAdminEntity(uuid));
        context.getGameStateService().addAdmin(new PracticeAdminEntity(uuid));
    }
    
    @Override
    public void removeAdmin(UUID adminUniqueId) throws SQLException {
        context.getGameState().removeAdmin(adminUniqueId);
        String uuid = adminUniqueId.toString();
        context.getGameStateService().deleteActiveAdmin(uuid);
        context.getGameStateService().deletePracticeAdmin(uuid);
    }
}
