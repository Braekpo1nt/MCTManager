package org.braekpo1nt.mctmanager.games.gamestate.states;

import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.database.entities.admin.ActiveAdminEntity;
import org.braekpo1nt.mctmanager.games.gamestate.GameStateStorageUtil;
import org.braekpo1nt.mctmanager.games.gamestate.MCTPlayerEntity;
import org.braekpo1nt.mctmanager.games.gamestate.MCTTeamEntity;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.logging.Level;

public abstract class StorageUtilState {
    
    protected final @NotNull GameStateStorageUtil context;
    
    public StorageUtilState(@NotNull GameStateStorageUtil context) {
        this.context = context;
    }
    
    public abstract void enter();
    
    public abstract void exit();
    
    /**
     * @param teamId the teamId
     * @param teamDisplayName the display name of the team
     * @param color the color string of the team
     * @return the score of the team (based on historical values)
     */
    public CompletableFuture<Integer> addTeam(String teamId, String teamDisplayName, String color) {
        MCTTeamEntity team = context.getGameState().addTeam(teamId, teamDisplayName, color);
        return CompletableFuture.supplyAsync(() -> {
                    try {
                        int score = persistAddTeam(teamId, team);
                        team.setScore(score);
                        return score;
                    } catch (SQLException e) {
                        throw new CompletionException(e);
                    }
                }, context.getDatabaseExecutor())
                .exceptionally(e -> {
                    Main.logger().log(Level.SEVERE, String.format("unable to persist team with id %s", teamId), e);
                    return 0;
                });
    }
    
    protected abstract int persistAddTeam(String teamId, MCTTeamEntity team) throws SQLException;
    
    public CompletableFuture<Void> removeTeam(String teamId) {
        List<UUID> uuidsOnTeam = context.getParticipantUUIDsOnTeam(teamId);
        context.getGameState().removePlayers(uuidsOnTeam);
        context.getGameState().removeTeam(teamId);
        return CompletableFuture.runAsync(() -> {
                    try {
                        context.getGameStateService().deleteTeam(teamId);
                        persistRemoveTeam(teamId);
                    } catch (SQLException e) {
                        Main.logger().log(Level.SEVERE, String.format("Unable to delete team with id %s", teamId));
                        throw new CompletionException(e);
                    }
                }, context.getDatabaseExecutor())
                .exceptionally(e -> {
                    Main.logger().log(Level.SEVERE, String.format("unable to remove team with teamId %s", teamId), e);
                    return null;
                });
    }
    
    protected abstract void persistRemoveTeam(String teamId) throws SQLException;
    
    /**
     * @param uuid the UUID of the player to join
     * @param ign the ign of the player to join
     * @param teamId the teamId to join them to
     * @return the score of the participant (based on historical values)
     */
    public CompletableFuture<Integer> joinPlayer(@NotNull UUID uuid, @NotNull String ign, @NotNull String teamId) {
        MCTPlayerEntity player = context.getGameState().addPlayer(uuid, ign, teamId);
        return CompletableFuture.supplyAsync(() -> {
                    try {
                        int score = persistJoinPlayer(uuid, player);
                        player.setScore(score);
                        return score;
                    } catch (SQLException e) {
                        throw new CompletionException(e);
                    }
                }, context.getDatabaseExecutor())
                .exceptionally(e -> {
                    Main.logger().log(Level.SEVERE, String.format("unable to persist player with UUID %s, ign %s, and teamId %s", uuid, ign, teamId), e);
                    return 0;
                });
    }
    
    protected abstract int persistJoinPlayer(@NotNull UUID uuid, MCTPlayerEntity player) throws SQLException;
    
    public CompletableFuture<Void> leavePlayer(@NotNull UUID uuid) {
        context.getGameState().removePlayer(uuid);
        return CompletableFuture.runAsync(() -> {
                    try {
                        String uuidString = uuid.toString();
                        context.getGameStateService().deleteParticipant(uuidString);
                        persistLeavePlayer(uuidString);
                    } catch (SQLException e) {
                        throw new CompletionException(e);
                    }
                }, context.getDatabaseExecutor())
                .exceptionally(e -> {
                    Main.logger().log(Level.SEVERE, String.format("unable to leave player with UUID %s", uuid), e);
                    return null;
                });
    }
    
    public abstract void persistLeavePlayer(@NotNull String uuidString) throws SQLException;
    
    public CompletableFuture<Void> addAdmin(UUID uuid) {
        context.getGameState().addAdmin(uuid);
        return CompletableFuture.runAsync(() -> {
                    try {
                        String uuidString = uuid.toString();
                        context.getGameStateService().addAdmin(new ActiveAdminEntity(uuidString));
                        persistAddAdmin(uuidString);
                    } catch (SQLException e) {
                        throw new CompletionException(e);
                    }
                }, context.getDatabaseExecutor())
                .exceptionally(e -> {
                    Main.logger().log(Level.SEVERE, String.format("unable to add admin with UUID %s", uuid), e);
                    return null;
                });
    }
    
    
    public abstract void persistAddAdmin(@NotNull String uuid) throws SQLException;
    
    public CompletableFuture<Void> removeAdmin(UUID uuid) {
        context.getGameState().removeAdmin(uuid);
        return CompletableFuture.runAsync(() -> {
                    try {
                        String uuidString = uuid.toString();
                        context.getGameStateService().deleteActiveAdmin(uuidString);
                        persistRemoveAdmin(uuidString);
                    } catch (SQLException e) {
                        throw new CompletionException(e);
                    }
                }, context.getDatabaseExecutor())
                .exceptionally(e -> {
                    Main.logger().log(Level.SEVERE, String.format("unable to remove admin with UUID %s", uuid), e);
                    return null;
                });
    }
    
    public abstract void persistRemoveAdmin(@NotNull String uuid) throws SQLException;
    
    public @NotNull CompletableFuture<Map<UUID, String>> getAllAdminNames() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return getAdminNames();
            } catch (SQLException e) {
                Main.logger().log(Level.SEVERE, "Unable get admin names from database", e);
                return Collections.emptyMap();
            }
        }, context.getDatabaseExecutor());
    }
    
    protected abstract @NotNull Map<UUID, String> getAdminNames() throws SQLException;
}
