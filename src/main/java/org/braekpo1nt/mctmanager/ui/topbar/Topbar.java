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
    
    public void showPlayer(@NotNull Player player) {
        FormattedBar bossBar = new FormattedBar(player);
        bossBar.show();
        bossBars.put(player.getUniqueId(), bossBar);
    }
    
    public void hidePlayer(@NotNull Player player) {
        FormattedBar bossBar = bossBars.remove(player.getUniqueId());
        Preconditions.checkArgument(bossBar != null, "player with UUID \"%s\" does not exist in this BattleTopbar", player.getUniqueId());
        bossBar.hide();
    }
    
    public void hidePlayers(@NotNull List<@NotNull Player> players) {
        for (Player player : players) {
            this.hidePlayer(player);
        }
    }
    
    public void setLeft(@NotNull Component left) {
        for (FormattedBar bossBar : bossBars.values()) {
            bossBar.setLeft(left);
        }
    }
    
    public void setLeft(@NotNull UUID playerUUID, @NotNull Component left) {
        FormattedBar bossBar = bossBars.get(playerUUID);
        Preconditions.checkArgument(bossBar != null, "player with UUID \"%s\" does not exist in this BattleTopbar", playerUUID);
        bossBar.setLeft(left);
    }
    
    public void setMiddle(@NotNull Component middle) {
        for (FormattedBar bossBar : bossBars.values()) {
            bossBar.setMiddle(middle);
        }
    }
    
    public void setMiddle(@NotNull UUID playerUUID, @NotNull Component middle) {
        FormattedBar bossBar = bossBars.get(playerUUID);
        Preconditions.checkArgument(bossBar != null, "player with UUID \"%s\" does not exist in this BattleTopbar", playerUUID);
        bossBar.setMiddle(middle);
    }
    
    public void setRight(@NotNull Component right) {
        for (FormattedBar bossBar : bossBars.values()) {
            bossBar.setRight(right);
        }
    }
    
    public void setRight(@NotNull UUID playerUUID, @NotNull Component right) {
        FormattedBar bossBar = bossBars.get(playerUUID);
        Preconditions.checkArgument(bossBar != null, "player with UUID \"%s\" does not exist in this BattleTopbar", playerUUID);
        bossBar.setRight(right);
    }
    
}
