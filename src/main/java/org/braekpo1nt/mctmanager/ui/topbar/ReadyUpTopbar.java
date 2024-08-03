package org.braekpo1nt.mctmanager.ui.topbar;

import com.google.common.base.Preconditions;
import lombok.Data;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.braekpo1nt.mctmanager.ui.topbar.components.PlayerReadyUpComponent;
import org.braekpo1nt.mctmanager.ui.topbar.components.TeamsReadyUpComponent;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * An implementation of a Topbar specifically oriented toward teams ready-ing up 
 * (e.g. for an event)
 */
// TODO: implement this
public class ReadyUpTopbar implements Topbar {
    
    @Data
    protected static class TeamData {
        /**
         * how many players on the team are ready
         */
        private long readyCount;
        /**
         * how many players are on the team
         */
        private long size;   
    }
    
    @Data
    protected static class PlayerData {
        private final @NotNull FormattedBar bossBar;
        private final PlayerReadyUpComponent playerReadyUpComponent = new PlayerReadyUpComponent();
    }
    
    private final TeamsReadyUpComponent teamsReadyUpComponent = new TeamsReadyUpComponent();
    /**
     * each team's TeamData
     */
    private final Map<String, TeamData> teamDatas = new HashMap<>();
    /**
     * each player's PlayerData
     */
    private final Map<UUID, PlayerData> playerDatas = new HashMap<>();
    
    private @NotNull TeamData getTeamData(@NotNull String teamId) {
        TeamData teamData = teamDatas.get(teamId);
        Preconditions.checkArgument(teamData != null, "team with teamId \"%s\" does not exist in this ReadyUpTopbar", teamId);
        return teamData;
    }
    
    private @NotNull PlayerData getPlayerData(@NotNull UUID playerUUID) {
        PlayerData playerData = playerDatas.get(playerUUID);
        Preconditions.checkArgument(playerData != null, "player with UUID \"%s\" does not exist in this ReadyUpTopbar", playerUUID);
        return playerData;
    }
    
    /**
     * Update every player's left {@link TeamsReadyUpComponent}
     */
    private void update() {
        for (PlayerData playerData : playerDatas.values()) {
            playerData.getBossBar().setLeft(teamsReadyUpComponent.toComponent());
        }
    }
    
    /**
     * Update the given PlayerData
     * @param playerData the PlayerData to update
     */
    private void update(@NotNull PlayerData playerData) {
        playerData.getBossBar().setLeft(teamsReadyUpComponent.toComponent());
        PlayerReadyUpComponent playerReadyUpComponent = playerData.getPlayerReadyUpComponent();
        playerData.getBossBar().setRight(playerReadyUpComponent.toComponent());
    }
    
    /**
     * Add a new teamId to this Topbar. 
     * @param teamId the teamId to add. Must not already exist in this Topbar
     * @param teamColor the color of the team
     */
    public void addTeam(@NotNull String teamId, @NotNull TextColor teamColor) {
        TeamData newTeamData = new TeamData();
        teamDatas.put(teamId, newTeamData);
        teamsReadyUpComponent.addTeam(teamId, teamColor);
        update();
    }
    
    /**
     * Removes all teams from this Topbar
     */
    public void removeAllTeams() {
        teamDatas.clear();
        teamsReadyUpComponent.removeAllTeams();
        update();
    }
    
    /**
     * @param teamId a valid teamId in this Topbar
     * @param readyCount the number of ready participants on this team. Negative number indicates
     *                   the team is fully ready. 
     */
    public void setReadyCount(@NotNull String teamId, long readyCount) {
        TeamData teamData = getTeamData(teamId);
        teamData.setReadyCount(readyCount);
        teamsReadyUpComponent.setReadyCount(teamId, readyCount);
        update();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void showPlayer(@NotNull Player player) {
        Preconditions.checkArgument(!playerDatas.containsKey(player.getUniqueId()), "player with UUID \"%s\" already exists in this ManyBattleTopbar", player.getUniqueId());
        FormattedBar bossBar = new FormattedBar(player);
        bossBar.show();
        PlayerData newPlayerData = new PlayerData(bossBar);
        playerDatas.put(player.getUniqueId(), newPlayerData);
        update(newPlayerData);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void hidePlayer(@NotNull UUID playerUUID) {
        PlayerData playerData = playerDatas.remove(playerUUID);
        Preconditions.checkArgument(playerData != null, "player with UUID \"%s\" does not exist in this BattleTopbar", playerUUID);
        playerData.getBossBar().hide();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void hidePlayers(@NotNull List<@NotNull UUID> playerUUIDs) {
        for (UUID playerUUID : playerUUIDs) {
            hidePlayer(playerUUID);
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void hideAllPlayers() {
        for (PlayerData playerData : playerDatas.values()) {
            playerData.getBossBar().hide();
        }
        playerDatas.clear();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void setLeft(@NotNull Component left) {
        for (PlayerData playerData : playerDatas.values()) {
            playerData.getBossBar().setLeft(left);
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void setLeft(@NotNull UUID playerUUID, @NotNull Component left) {
        PlayerData playerData = getPlayerData(playerUUID);
        playerData.getBossBar().setLeft(left);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void setMiddle(@NotNull Component middle) {
        for (PlayerData playerData : playerDatas.values()) {
            playerData.getBossBar().setMiddle(middle);
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void setMiddle(@NotNull UUID playerUUID, @NotNull Component middle) {
        PlayerData playerData = getPlayerData(playerUUID);
        playerData.getBossBar().setMiddle(middle);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void setRight(@NotNull Component right) {
        for (PlayerData playerData : playerDatas.values()) {
            playerData.getBossBar().setRight(right);
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void setRight(@NotNull UUID playerUUID, @NotNull Component right) {
        PlayerData playerData = getPlayerData(playerUUID);
        playerData.getBossBar().setRight(right);
    }
    
    /**
     * Set the player's ready status
     * @param playerUUID the player to assign. Must exist in this Topbar
     * @param ready true if the player is ready, false if the player is not ready,
     *              null if the player should not display a ready status.
     */
    public void setReady(@NotNull UUID playerUUID, Boolean ready) {
        PlayerData playerData = getPlayerData(playerUUID);
        playerData.getPlayerReadyUpComponent().setReady(ready);
        update(playerData);
    }
}
