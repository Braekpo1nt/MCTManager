package org.braekpo1nt.mctmanager.games.utils;

import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.participant.Participant;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Objects;

public class ParticipantInitializer {
    
    //TODO: remove this in favor of death+respawn combination
    private static Main plugin;
    
    /**
     * this is needed until you complete issue #
     * @param plugin the plugin
     */
    public static void setPlugin(Main plugin) {
        ParticipantInitializer.plugin = plugin;
    }
    
    /**
     * Removes all potion effects from the participant, and extinguishes fire if the participant is on fire
     * @param participant The participant
     */
    public static void clearStatusEffects(Player participant) {
        for (PotionEffect effect : participant.getActivePotionEffects()) {
            if (!effect.getType().equals(PotionEffectType.NIGHT_VISION)) {
                participant.removePotionEffect(effect.getType());
            }
        }
        extinguishFire(participant);
        if (plugin != null) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> extinguishFire(participant), 2L);
        }
        participant.setArrowsInBody(0);
    }
    public static void clearStatusEffects(Participant participant) {
        clearStatusEffects(participant.getPlayer());
    }
    
    private static void extinguishFire(Player participant) {
        participant.setFireTicks(0);
        participant.setVisualFire(false);
    }
    private static void extinguishFire(Participant participant) {
        extinguishFire(participant.getPlayer());
    }
    
    public static void resetHealthAndHunger(Player participant) {
        participant.setHealth(Objects.requireNonNull(participant.getAttribute(Attribute.MAX_HEALTH)).getDefaultValue());
        participant.setFoodLevel(20);
        participant.setSaturation(5);
    }
    public static void resetHealthAndHunger(Participant participant) {
        resetHealthAndHunger(participant.getPlayer());
    }
    
    public static void clearInventory(Player participant) {
        participant.getInventory().clear();
        participant.getOpenInventory().setCursor(new ItemStack(Material.AIR));
    }
    public static void clearInventory(Participant participant) {
        clearInventory(participant.getPlayer());
    }
}
