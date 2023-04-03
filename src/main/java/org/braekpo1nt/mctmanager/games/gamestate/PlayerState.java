package org.braekpo1nt.mctmanager.games.gamestate;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

import java.util.Collection;
import java.util.Collections;

/**
 * Used to store the state of a player mid-game when they leave or join.
 * 
 * @deprecated This might not be needed. Not sure if there are games where you need
 * to store these things, or maybe it's just simpler to store it in a Player object. 
 */
@Deprecated(since = "4/2/2023", forRemoval = false)
public class PlayerState {
    private final ItemStack[] inventoryContents;
    private final ItemStack[] armorContents;
    private final double health;
    private final int foodLevel;
    private final float saturation;
    private final Collection<PotionEffect> activePotionEffects;
    private final int level;
    private final float exp;
    
    /**
     * The default state of a player. Essentially like respawning with a fresh
     * start. 
     */
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
    
    /**
     * Instantiate a new PlayerState with the specified elements.
     * 
     * @param inventoryContents
     * @param armorContents
     * @param health
     * @param foodLevel
     * @param saturation
     * @param activePotionEffects
     * @param level
     * @param exp
     */
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
    
    /**
     * Instantiates a new PlayerState with the state of the given player
     * @param player The player whose state to save. 
     */
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
    
    /**
     * Replace the given player's state with this PlayerState
     * @param player The player whose state you are replacing with this state
     */
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
