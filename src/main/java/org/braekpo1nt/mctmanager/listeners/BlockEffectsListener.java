package org.braekpo1nt.mctmanager.listeners;

import org.braekpo1nt.mctmanager.Main;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class BlockEffectsListener implements Listener {
    
    private boolean blockEffectsEnabled = true;
    
    public BlockEffectsListener(Main plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }
    
    @EventHandler
    public void giveEffects(PlayerMoveEvent event) {
        if (!blockEffectsEnabled) {
            return;
        }
        Player player = event.getPlayer();
        Material standingBlock = player.getLocation().add(0, -1, 0).getBlock().getType();
        switch (standingBlock) {
            case MAGENTA_GLAZED_TERRACOTTA:
                player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 80, 7, true, false, false));
                break;
            case LIME_GLAZED_TERRACOTTA:
                player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 140, 4, true, false, false));
                break;
            case BEDROCK:
                player.getActivePotionEffects().stream().forEach(potionEffect -> player.removePotionEffect(potionEffect.getType()));
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
