package org.braekpo1nt.mctmanager.games.utils;

import org.braekpo1nt.mctmanager.Main;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

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
        Bukkit.getScheduler().runTaskLater(plugin, () -> extinguishFire(participant), 2L);
        participant.setArrowsInBody(0);
    }
    
    private static void extinguishFire(Player participant) {
        participant.setFireTicks(0);
        participant.setVisualFire(false);
    }
    
    public static void resetHealthAndHunger(Player participant) {
        participant.setHealth(participant.getAttribute(Attribute.GENERIC_MAX_HEALTH).getDefaultValue());
        participant.setFoodLevel(20);
        participant.setSaturation(5);
    }
}
