package org.braekpo1nt.mctmanager.games.game.farmrush.config;

import lombok.Data;
import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.config.dto.org.bukkit.inventory.recipes.RecipeDTO;
import org.braekpo1nt.mctmanager.config.validation.Validatable;
import org.braekpo1nt.mctmanager.config.validation.Validator;
import org.braekpo1nt.mctmanager.games.game.farmrush.powerups.PowerupManager;
import org.braekpo1nt.mctmanager.games.game.farmrush.powerups.PowerupType;
import org.braekpo1nt.mctmanager.games.game.farmrush.powerups.specs.CropGrowerSpec;
import org.braekpo1nt.mctmanager.io.IOUtils;
import org.braekpo1nt.mctmanager.ui.UIUtils;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;

@Data
class CropGrowerSpecDTO implements Validatable {
    private final PowerupType type = PowerupType.CROP_GROWER;
    /**
     * The recipe used to craft this powerup. Can't be null. If the result is specified
     * in the config, it will be overwritten by the internal powerup.
     */
    private RecipeDTO recipe;
    /**
     * the radius of the effect range.
     */
    private double radius;
    /**
     * the custom model data to apply to the item. Defaults to 0.
     */
    private int customModelData = 0;
    /**
     * How many ticks pass between probability checks
     * If this is {@code x}, then each block will have a {@link #growthChance}
     * chance to grow to the next cycle every {@code x} seconds.
     * Defaults to 20, can't be negative.
     */
    private long ticksPerCycle = 20;
    /**
     * the chance per probability check for a crop to grow to the next age.
     * A probability check happens every {@link #ticksPerCycle} seconds
     * Defaults to 1.0, must be between 0.0 and 1.0 inclusive.
     */
    private double growthChance = 1.0;
    /**
     * An image showing the player the recipe for this powerup
     */
    private @Nullable String recipeImage;
    
    // Particles start
    /**
     * How many ticks pass between each particle spawn cycle.
     * Defaults to 20
     */
    private long ticksPerParticleCycle = 20L;
    /**
     * which particle spawns (defaults to {@link Particle#HAPPY_VILLAGER})
     */
    private Particle particle = Particle.HAPPY_VILLAGER;
    /**
     * how many groups of particles are spawned per spawn cycle
     * Defaults to 1.
     */
    private int numberOfParticles = 1;
    /**
     * the standard "number of particles" number for spawning a single particle,
     * the same as you would expect from the default minecraft command.
     * Defaults to 1.
     */
    private int particleCount = 1;
    // Particles end
    
    /**
     * @param world trivial world used for creating a map. If null, no map item is created
     * @return the specified spec
     */
    public CropGrowerSpec toSpec(World world) {
        ItemStack cropGrowerItem = PowerupManager.cropGrowerItem;
        cropGrowerItem.editMeta(meta -> meta.setCustomModelData(customModelData));
        ItemStack newRecipeMap = null;
        if (recipeImage != null && world != null) {
            try {
                newRecipeMap = UIUtils.createMapItem(world, new File(recipeImage));
                newRecipeMap.editMeta(meta -> {
                    meta.displayName(Component.text("Crop Grower Recipe"));
                });
            } catch (IOException ignored) {
            }
        }
        return CropGrowerSpec.builder()
                .recipe(recipe.toRecipe(cropGrowerItem))
                .recipeKey(recipe.getNamespacedKey())
                .radius(radius)
                .ticksPerCycle(ticksPerCycle)
                .growthChance(growthChance)
                .recipeMap(newRecipeMap)
                // Particles start
                .ticksPerParticleCycle(ticksPerParticleCycle)
                .particle(particle)
                .numberOfParticles(numberOfParticles)
                .particleCount(particleCount)
                // Particles end
                .build();
    }
    
    @Override
    public void validate(@NotNull Validator validator) {
        validator.notNull(recipe, "recipe");
        validator.validate(radius >= 0.0, "radius can't be negative");
        validator.validate(ticksPerCycle >= 1, "ticksPerCycle must be at least 1");
        validator.validate(0 <= growthChance && growthChance <= 1.0, "growthChance must be between 0.0 and 1.0 inclusive");
        if (recipeImage != null) {
            File recipeImageFile = new File(recipeImage);
            validator.fileExists(recipeImage, "recipeImage");
            try {
                IOUtils.toBufferedImage(recipeImageFile);
            } catch (IOException e) {
                validator.invalid("recipeImage could not be read as an image.");
            }
        }
        
        // Particles start
        validator.validate(ticksPerParticleCycle >= 1L, "ticksPerParticleCycle must be at least 1");
        validator.notNull(particle, "particle");
        validator.validate(numberOfParticles >= 1, "numberOfParticles must be at least 1");
        validator.validate(particleCount >= 1, "particleCount must be at least 1");
        // Particles end
    }
}
