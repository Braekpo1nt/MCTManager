package org.braekpo1nt.mctmanager.ui.topbar;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class Topbar {
    
    private final Map<UUID, IndividualBar> bossBars = new HashMap<>();
    
    public void addPlayer(@NotNull Player player) {
        IndividualBar bossBar = new IndividualBar();
        bossBar.show(player);
        bossBars.put(player.getUniqueId(), bossBar);
    }
    
    public void removePlayer(@NotNull Player player) {
        IndividualBar bossBar = bossBars.remove(player.getUniqueId());
        if (bossBar != null) {
            bossBar.hide(player);
        }
    }
    
    public void removePlayers(@NotNull List<@NotNull Player> players) {
        for (Player player : players) {
            this.removePlayer(player);
        }
    }
    
    public void setLeft(@NotNull Component left) {
        for (IndividualBar bossBar : bossBars.values()) {
            bossBar.setLeft(left);
        }
    }
    
    public void setLeft(@NotNull UUID playerUUID, @NotNull Component left) {
        IndividualBar bossBar = bossBars.get(playerUUID);
        if (bossBar != null) {
            bossBar.setLeft(left);
        }
    }
    
    public void setMiddle(@NotNull Component middle) {
        for (IndividualBar bossBar : bossBars.values()) {
            bossBar.setMiddle(middle);
        }
    }
    
    public void setMiddle(@NotNull UUID playerUUID, @NotNull Component middle) {
        IndividualBar bossBar = bossBars.get(playerUUID);
        if (bossBar != null) {
            bossBar.setMiddle(middle);
        }
    }
    
    public void setRight(@NotNull Component right) {
        for (IndividualBar bossBar : bossBars.values()) {
            bossBar.setRight(right);
        }
    }
    
    public void setRight(@NotNull UUID playerUUID, @NotNull Component right) {
        IndividualBar bossBar = bossBars.get(playerUUID);
        if (bossBar != null) {
            bossBar.setRight(right);
        }
    }
    
}
