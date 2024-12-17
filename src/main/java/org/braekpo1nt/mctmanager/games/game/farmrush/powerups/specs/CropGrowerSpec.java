package org.braekpo1nt.mctmanager.games.game.farmrush.powerups.specs;

import lombok.Builder;
import lombok.Data;
import org.braekpo1nt.mctmanager.games.game.farmrush.powerups.CropGrower;
import org.braekpo1nt.mctmanager.games.game.farmrush.powerups.PowerupManager;
import org.braekpo1nt.mctmanager.games.game.farmrush.powerups.PowerupType;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Changes the speed of crop growth in a range
 */
@Data
@Builder
public class CropGrowerSpec implements PowerupSpec {
    private final PowerupType type = PowerupType.CROP_GROWER;
    
    private final @NotNull ItemStack cropGrowerItem;
    private final @NotNull Recipe recipe;
    private double radius;
    private NamespacedKey recipeKey;
    
    /**
     * how many ticks between each grow attempt. Each cycle runs a probability check
     * on the crops in its radius to see if it should grow.
     * Defaults to 20.
     */
    @Builder.Default
    private long ticksPerCycle = 20L;
    private double growthChance;
    /**
     * An image showing the player the recipe for this powerup
     */
    private @Nullable ItemStack recipeMap;
    
    // Particles start
    /**
     * How many ticks pass between each particle spawn cycle.
     */
    private long ticksPerParticleCycle;
    /**
     * which particle spawns
     */
    private Particle particle;
    /**
     * how many groups of particles are spawned per spawn cycle
     */
    private int numberOfParticles;
    /**
     * the standard "number of particles" number for spawning a single particle,
     * the same as you would expect from the default minecraft command
     */
    private int particleCount;
    // Particles end
    
    public CropGrower createPowerup(Location location) {
        return new CropGrower(location, radius, growthChance);
    }
    
    @Override
    public boolean isItem(@Nullable ItemStack itemStack) {
        if (itemStack == null) {
            return false;
        }
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta == null) {
            return false;
        }
        return cropGrowerItem.getItemMeta().equals(itemMeta);
    }
}
