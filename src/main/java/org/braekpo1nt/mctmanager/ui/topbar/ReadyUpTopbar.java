package org.braekpo1nt.mctmanager.ui.topbar;

import lombok.Data;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.braekpo1nt.mctmanager.ui.UIUtils;
import org.braekpo1nt.mctmanager.ui.topbar.components.PlayerReadyUpComponent;
import org.braekpo1nt.mctmanager.ui.topbar.components.TeamsReadyUpComponent;
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
public class ReadyUpTopbar implements Topbar {
    
    @Data
    protected static class PlayerData {
        private final @NotNull FormattedBar bossBar;
        private final PlayerReadyUpComponent playerReadyUpComponent = new PlayerReadyUpComponent();
    }
    
    private final TeamsReadyUpComponent teamsReadyUpComponent = new TeamsReadyUpComponent();
    /**
     * each player's PlayerData
     */
    private final Map<UUID, PlayerData> playerDatas = new HashMap<>();
    
    private @Nullable PlayerData getPlayerData(@NotNull UUID playerUUID) {
        PlayerData playerData = playerDatas.get(playerUUID);
        if (playerData == null) {
            UIUtils.logUIError("player with UUID \"%s\" does not exist in this ReadyUpTopbar", playerUUID);
        }
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
        teamsReadyUpComponent.addTeam(teamId, teamColor);
        update();
    }
    
    /**
     * Removes all teams from this Topbar
     * @deprecated use {@link #cleanup()}
     */
    @Deprecated
    public void removeAllTeams() {
        teamsReadyUpComponent.removeAllTeams();
        update();
    }
    
    /**
     * @param teamId a valid teamId in this Topbar
     * @param readyCount the number of ready participants on this team. Negative number indicates
     * the team is fully ready.
     */
    public void setReadyCount(@NotNull String teamId, long readyCount) {
        teamsReadyUpComponent.setReadyCount(teamId, readyCount);
        update();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void showPlayer(@NotNull Player player) {
        if (playerDatas.containsKey(player.getUniqueId())) {
            UIUtils.logUIError("player with UUID \"%s\" already exists in this ManyBattleTopbar", player.getUniqueId());
            return;
        }
        FormattedBar bossBar = new FormattedBar(player);
        bossBar.show();
        PlayerData newPlayerData = new PlayerData(bossBar);
        playerDatas.put(player.getUniqueId(), newPlayerData);
        update(newPlayerData);
    }
    
    @Override
    public void cleanup() {
        removeAllTeams();
        hideAllPlayers();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void hidePlayer(@NotNull UUID playerUUID) {
        PlayerData playerData = playerDatas.remove(playerUUID);
        if (playerData == null) {
            UIUtils.logUIError("player with UUID \"%s\" does not exist in this BattleTopbar", playerUUID);
            return;
        }
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
     * @deprecated use {@link #cleanup()}
     */
    @Deprecated
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
        if (playerData == null) {
            return;
        }
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
        if (playerData == null) {
            return;
        }
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
        if (playerData == null) {
            return;
        }
        playerData.getBossBar().setRight(right);
    }
    
    /**
     * Set the player's ready status
     * @param playerUUID the player to assign. Must exist in this Topbar
     * @param ready true if the player is ready, false if the player is not ready,
     * null if the player should not display a ready status.
     */
    public void setReady(@NotNull UUID playerUUID, Boolean ready) {
        PlayerData playerData = getPlayerData(playerUUID);
        if (playerData == null) {
            return;
        }
        playerData.getPlayerReadyUpComponent().setReady(ready);
        update(playerData);
    }
}
