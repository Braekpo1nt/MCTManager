package org.braekpo1nt.mctmanager.ui.topbar;

import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.ui.UIUtils;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Represents a custom BossBar display for one or more players. Can update the information
 * displayed for all players at once, or for individual players. 
 */
public class BasicTopbar implements Topbar {
    
    private final Map<UUID, PlayerData> playerDatas = new HashMap<>();
    
    /**
     * @param playerUUID the UUID of the PlayerData. Must be a valid key in {@link BasicTopbar#playerDatas}
     * @return the {@link PlayerData} associated with this UUID
     */
    protected @Nullable PlayerData getPlayerData(@NotNull UUID playerUUID) {
        PlayerData playerData = playerDatas.get(playerUUID);
        if (playerData == null) {
            UIUtils.logUIError("player with UUID \"%s\" does not exist in this BattleTopbar", playerUUID);
        }
        return playerData;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void showPlayer(@NotNull Player player) {
        if (playerDatas.containsKey(player.getUniqueId())) {
            UIUtils.logUIError("player with UUID \"%s\" already exists in this BatleTopbar", player.getUniqueId());
            return;
        }
        FormattedBar bossBar = new FormattedBar(player);
        bossBar.show();
        playerDatas.put(player.getUniqueId(), new PlayerData(bossBar));
    }
    
    @Override
    public void cleanup() {
        this.hideAllPlayers();
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
            this.hidePlayer(playerUUID);
        }
    }
    
    /**
     * Hides the BossBar display from all players in this Topbar
     */
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
}
