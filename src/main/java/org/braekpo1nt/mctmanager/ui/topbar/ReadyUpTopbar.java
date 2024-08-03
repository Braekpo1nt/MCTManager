package org.braekpo1nt.mctmanager.ui.topbar;

import com.google.common.base.Preconditions;
import lombok.Data;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.braekpo1nt.mctmanager.ui.topbar.components.ReadyUpComponent;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
        private long readyCount;
    }
    
    @Data
    protected static class PlayerData {
        private final @NotNull FormattedBar bossBar;
    }
    
    private final ReadyUpComponent readyUpComponent = new ReadyUpComponent();
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
     * Update every player's left {@link ReadyUpComponent}
     */
    private void update() {
        for (PlayerData playerData : playerDatas.values()) {
            playerData.getBossBar().setLeft(readyUpComponent.toComponent());
        }
    }
    
    /**
     * Update the given PlayerData
     * @param playerData the PlayerData to update
     */
    private void update(@NotNull PlayerData playerData) {
        playerData.getBossBar().setLeft(readyUpComponent.toComponent());
    }
    
    /**
     * Add a new teamId to this Topbar. 
     * @param teamId the teamId to add. Must not already exist in this Topbar
     * @param teamColor the color of the team
     */
    public void addTeam(@NotNull String teamId, @NotNull TextColor teamColor) {
        TeamData newTeamData = new TeamData();
        teamDatas.put(teamId, newTeamData);
        readyUpComponent.addTeam(teamId, teamColor);
        update();
    }
    
    /**
     * Removes all teams from this Topbar
     */
    public void removeAllTeams() {
        teamDatas.clear();
        readyUpComponent.removeAllTeams();
        update();
    }
    
    public void setReadyCount(@NotNull String teamId, long readyCount) {
        Preconditions.checkArgument(readyCount >= 0, "readyCount can't be negative");
        TeamData teamData = getTeamData(teamId);
        teamData.setReadyCount(readyCount);
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
}
