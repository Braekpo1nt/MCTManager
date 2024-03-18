package org.braekpo1nt.mctmanager.listeners;

import org.braekpo1nt.mctmanager.Main;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * Handles listening for players to walk on special blocks and giving them appropriate status effects. 
 */
public class BlockEffectsListener implements Listener {
    
    private boolean blockEffectsEnabled = true;
    
    private final PotionEffect SPEED = new PotionEffect(PotionEffectType.SPEED, 80, 5, true, false, false);
    private final PotionEffect JUMP = new PotionEffect(PotionEffectType.JUMP, 140, 7, true, false, false);
    
    public BlockEffectsListener(Main plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }
    
    @EventHandler
    public void giveEffects(PlayerMoveEvent event) {
        if (!blockEffectsEnabled) {
            return;
        }
        Player player = event.getPlayer();
        if (player.getGameMode() == GameMode.SPECTATOR) {
            return;
        }
        Material standingBlock = player.getLocation().add(0, -1, 0).getBlock().getType();
        switch (standingBlock) {
            case MAGENTA_GLAZED_TERRACOTTA:
                player.addPotionEffect(SPEED);
                break;
            case LIME_GLAZED_TERRACOTTA:
                player.addPotionEffect(JUMP);
                break;
            case BEDROCK:
                player.removePotionEffect(PotionEffectType.JUMP);
                player.removePotionEffect(PotionEffectType.SPEED);
                break;
        }
    }
    
    public void disableBlockEffects() {
        blockEffectsEnabled = false;
    }
    
    public void enableBlockEffects() {
        blockEffectsEnabled = true;
    }
    
}
