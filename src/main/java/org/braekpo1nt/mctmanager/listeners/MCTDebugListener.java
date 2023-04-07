package org.braekpo1nt.mctmanager.listeners;

import org.braekpo1nt.mctmanager.Main;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class MCTDebugListener implements Listener {
    
    public MCTDebugListener(Main plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }
    
//    @EventHandler
//    public void onPickupFlag(PlayerMoveEvent event) {
//        Player player = event.getPlayer();
//        Block block = player.getLocation().getBlock();
//        if (block.getX() == 24 && block.getY() == -13 && block.getZ() == -1040) {
//            player.sendMessage("Picked up flag");
//        }
//    }
    
}
