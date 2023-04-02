package org.braekpo1nt.mctmanager.games;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

import java.util.Collection;
import java.util.Collections;

public class PlayerState {
    private final ItemStack[] inventoryContents;
    private final ItemStack[] armorContents;
    private final double health;
    private final int foodLevel;
    private final float saturation;
    private final Collection<PotionEffect> activePotionEffects;
    private final int level;
    private final float exp;
    
    public static final PlayerState DEFAULT = new PlayerState(
            new ItemStack[]{},
            new ItemStack[]{},
            20,
            20,
            5,
            Collections.emptyList(),
            0,
            0
    );
    
    private PlayerState(ItemStack[] inventoryContents, ItemStack[] armorContents, double health, int foodLevel, float saturation, Collection<PotionEffect> activePotionEffects, int level, float exp) {
        this.inventoryContents = inventoryContents;
        this.armorContents = armorContents;
        this.health = health;
        this.foodLevel = foodLevel;
        this.saturation = saturation;
        this.activePotionEffects = activePotionEffects;
        this.level = level;
        this.exp = exp;
    }
    
    public PlayerState(Player player) {
        this.inventoryContents = player.getInventory().getContents().clone();
        this.armorContents = player.getEquipment().getArmorContents().clone();
        this.health = player.getHealth();
        this.foodLevel = player.getFoodLevel();
        this.saturation = player.getSaturation();
        this.activePotionEffects = player.getActivePotionEffects();
        this.level = player.getLevel();
        this.exp = player.getExp();
    }
    
    public void setPlayerState(Player player) {
        player.getInventory().setContents(inventoryContents.clone());
        player.getEquipment().setArmorContents(armorContents.clone());
        player.setHealth(health);
        player.setFoodLevel(foodLevel);
        player.setSaturation(saturation);
        player.addPotionEffects(activePotionEffects);
        player.setLevel(level);
        player.setExp(exp);
    }
}
