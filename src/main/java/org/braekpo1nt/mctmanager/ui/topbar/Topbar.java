package org.braekpo1nt.mctmanager.ui.topbar;

import com.google.common.base.Preconditions;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Represents a custom BossBar display for one or more players. Can update the information
 * displayed for all players at once, or for individual players. 
 */
public class Topbar {
    
    protected final Map<UUID, FormattedBar> bossBars = new HashMap<>();
    
    /**
     * Show this Topbar to the given player
     * @param player the player to show this Topbar to
     */
    public void showPlayer(@NotNull Player player) {
        FormattedBar bossBar = new FormattedBar(player);
        bossBar.show();
        bossBars.put(player.getUniqueId(), bossBar);
    }
    
    /**
     * Hide the BossBar display from the given player
     * @param player the player to hide this Topbar from. Must be a valid player in this Topbar.
     */
    public void hidePlayer(@NotNull Player player) {
        FormattedBar bossBar = bossBars.remove(player.getUniqueId());
        Preconditions.checkArgument(bossBar != null, "player with UUID \"%s\" does not exist in this BattleTopbar", player.getUniqueId());
        bossBar.hide();
    }
    
    /**
     * Hide the BossBar display from all the given players
     * @param players the players to hide this Topbar from. Must be valid players in this Topbar.
     */
    public void hidePlayers(@NotNull List<@NotNull Player> players) {
        for (Player player : players) {
            this.hidePlayer(player);
        }
    }
    
    /**
     * Hides the BossBar display from all players in this Topbar
     */
    public void hideAllPlayers() {
        for (FormattedBar bossBar : bossBars.values()) {
            bossBar.hide();
        }
        bossBars.clear();
    }
    
    /**
     * Set the left display of the BattleTopbar to the given component
     * @param left the component to set the left section to
     */
    public void setLeft(@NotNull Component left) {
        for (FormattedBar bossBar : bossBars.values()) {
            bossBar.setLeft(left);
        }
    }
    
    /**
     * Set the left display of the BattleTopbar to the given component, specifically for one player
     * @param playerUUID the player to set the left section for
     * @param left the component to set the left section to
     */
    public void setLeft(@NotNull UUID playerUUID, @NotNull Component left) {
        FormattedBar bossBar = bossBars.get(playerUUID);
        Preconditions.checkArgument(bossBar != null, "player with UUID \"%s\" does not exist in this BattleTopbar", playerUUID);
        bossBar.setLeft(left);
    }
    
    /**
     * Set the middle display of the BattleTopbar to the given component
     * @param middle the component to set the middle section to
     */
    public void setMiddle(@NotNull Component middle) {
        for (FormattedBar bossBar : bossBars.values()) {
            bossBar.setMiddle(middle);
        }
    }
    
    /**
     * Set the middle display of the BattleTopbar to the given component, specifically for one player
     * @param playerUUID the player to set the middle section for
     * @param middle the component to set the middle section to
     */
    public void setMiddle(@NotNull UUID playerUUID, @NotNull Component middle) {
        FormattedBar bossBar = bossBars.get(playerUUID);
        Preconditions.checkArgument(bossBar != null, "player with UUID \"%s\" does not exist in this BattleTopbar", playerUUID);
        bossBar.setMiddle(middle);
    }
    
    /**
     * Set the right display of the BattleTopbar to the given component
     * @param right the component to set the right section to
     */
    public void setRight(@NotNull Component right) {
        for (FormattedBar bossBar : bossBars.values()) {
            bossBar.setRight(right);
        }
    }
    
    /**
     * Set the right display of the BattleTopbar to the given component, specifically for one player
     * @param playerUUID the player to set the right section for
     * @param right the component to set the right section to
     */
    public void setRight(@NotNull UUID playerUUID, @NotNull Component right) {
        FormattedBar bossBar = bossBars.get(playerUUID);
        Preconditions.checkArgument(bossBar != null, "player with UUID \"%s\" does not exist in this BattleTopbar", playerUUID);
        bossBar.setRight(right);
    }
    
}
