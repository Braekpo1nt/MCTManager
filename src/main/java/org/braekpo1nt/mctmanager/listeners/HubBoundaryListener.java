package org.braekpo1nt.mctmanager.listeners;

import com.onarandombox.MultiverseCore.MultiverseCore;
import com.onarandombox.MultiverseCore.api.MVWorldManager;
import com.onarandombox.MultiverseCore.api.MultiverseWorld;
import org.braekpo1nt.mctmanager.Main;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.Plugin;

public class HubBoundaryListener implements Listener {
    
    private boolean boundaryEnabled = true;
    
    public HubBoundaryListener(Main plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }
    
    /**
     * Detects when the player moves out of bounds of the hub, and teleports them back to the starting place
     * @param event A player move event
     */
    @EventHandler
    public void onPlayerOutofBounds(PlayerMoveEvent event) {
        if (boundaryEnabled) {
            Player player = event.getPlayer();
            Location loc = player.getLocation();

            Plugin multiversePlugin = Bukkit.getPluginManager().getPlugin("Multiverse-Core");
            MultiverseCore multiverseCore = ((MultiverseCore) multiversePlugin);
            MVWorldManager worldManager = multiverseCore.getMVWorldManager();
            MultiverseWorld hubWorld = worldManager.getMVWorld("Hub");
            MultiverseWorld playerWorld = worldManager.getMVWorld(player.getWorld());

            // make sure the player is in the hub world
            if (playerWorld.equals(hubWorld)) {
                // check if the player falls below y=130
                if (loc.getY() < 130) {
                    // Teleport player to hub start loc
                    player.teleport(hubWorld.getSpawnLocation());
                    player.sendMessage("You fell out of the hub boundary");
                }
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
