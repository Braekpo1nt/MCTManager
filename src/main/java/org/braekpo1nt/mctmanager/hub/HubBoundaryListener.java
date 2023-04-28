package org.braekpo1nt.mctmanager.hub;

import com.onarandombox.MultiverseCore.MultiverseCore;
import com.onarandombox.MultiverseCore.api.MVWorldManager;
import com.onarandombox.MultiverseCore.api.MultiverseWorld;
import org.braekpo1nt.mctmanager.Main;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.Plugin;

/**
 * Handles keeping players from walking off the side of the hub to their deaths
 */
public class HubBoundaryListener implements Listener {
    
    private final World hubWorld;
    private boolean boundaryEnabled = true;
    
    public HubBoundaryListener(Main plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        MVWorldManager worldManager = Main.multiverseCore.getMVWorldManager();
        this.hubWorld = worldManager.getMVWorld("Hub").getCBWorld();
    }
    
    /**
     * Detects when the player moves out of bounds of the hub, and teleports them back to the starting place
     * @param event A player move event
     */
    @EventHandler
    public void onPlayerOutOfBounds(PlayerMoveEvent event) {
        if (!boundaryEnabled) {
            return;
        }
        Player player = event.getPlayer();
        Location loc = player.getLocation();
        
        if (player.getWorld().equals(hubWorld)) {
            // check if the player falls below y=130
            if (loc.getY() < 130) {
                // Teleport player to hub start loc
                player.teleport(hubWorld.getSpawnLocation());
                player.sendMessage("You fell out of the hub boundary");
            }
        }
    }

    public void disableBoundary() {
        boundaryEnabled = false;
    }
    
    public void enableBoundary() {
        boundaryEnabled = true;
    }
}
