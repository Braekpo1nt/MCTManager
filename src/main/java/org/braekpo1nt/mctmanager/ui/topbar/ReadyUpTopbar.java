package org.braekpo1nt.mctmanager.ui.topbar;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

/**
 * An implementation of a Topbar specifically oriented toward teams ready-ing up 
 * (e.g. for an event)
 */
// TODO: implement this
public class ReadyUpTopbar implements Topbar {
    @Override
    public void showPlayer(@NotNull Player player) {
        
    }
    
    @Override
    public void hidePlayer(@NotNull UUID playerUUID) {
        
    }
    
    @Override
    public void hidePlayers(@NotNull List<@NotNull UUID> playerUUIDs) {
        
    }
    
    @Override
    public void hideAllPlayers() {
        
    }
    
    @Override
    public void setLeft(@NotNull Component left) {
        
    }
    
    @Override
    public void setLeft(@NotNull UUID playerUUID, @NotNull Component left) {
        
    }
    
    @Override
    public void setMiddle(@NotNull Component middle) {
        
    }
    
    @Override
    public void setMiddle(@NotNull UUID playerUUID, @NotNull Component middle) {
        
    }
    
    @Override
    public void setRight(@NotNull Component right) {
        
    }
    
    @Override
    public void setRight(@NotNull UUID playerUUID, @NotNull Component right) {
        
    }
}
