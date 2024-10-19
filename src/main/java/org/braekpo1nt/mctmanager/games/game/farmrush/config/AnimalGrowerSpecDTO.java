package org.braekpo1nt.mctmanager.games.game.farmrush.config;

import lombok.Data;
import org.braekpo1nt.mctmanager.config.dto.org.bukkit.inventory.recipes.RecipeDTO;
import org.braekpo1nt.mctmanager.config.validation.Validatable;
import org.braekpo1nt.mctmanager.config.validation.Validator;
import org.braekpo1nt.mctmanager.games.game.farmrush.powerups.PowerupManager;
import org.braekpo1nt.mctmanager.games.game.farmrush.powerups.PowerupType;
import org.braekpo1nt.mctmanager.games.game.farmrush.powerups.specs.AnimalGrowerSpec;
import org.bukkit.Particle;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;

@Data
class AnimalGrowerSpecDTO implements Validatable {
    
    private final PowerupType type = PowerupType.ANIMAL_GROWER;
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
     * How many ticks pass between checks for new entities in the radius
     */
    private long ticksPerCycle = 20L;
    /**
     * a growable mob's age is multiplied by this factor. To grow faster,
     * make it a number less than 1. E.g. a mob takes 20 ticks to grow, and
     * ageFactor is .75, it will take 15 ticks to grow when within the
     * {@link #radius}
     */
    private double ageMultiplier;
    /**
     * Works the same as {@link #ageMultiplier}, but for the breeding cooldown
     */
    private double breedMultiplier;
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
    
    public AnimalGrowerSpec toSpec() {
        ItemStack animalGrowerItem = PowerupManager.animalGrowerItem;
        animalGrowerItem.editMeta(meta -> meta.setCustomModelData(customModelData));
        
        return AnimalGrowerSpec.builder()
                .recipe(recipe.toRecipe(animalGrowerItem))
                .recipeKey(recipe.getNamespacedKey())
                .ticksPerCycle(ticksPerCycle)
                .radius(radius)
                .ageMultiplier(ageMultiplier)
                .breedMultiplier(breedMultiplier)
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
        validator.validate(ticksPerCycle >= 0, "ticksPerCycle can't be negative");
        validator.validate(ageMultiplier >= 0.0, "ageMultiplier can't be negative");
        validator.validate(breedMultiplier >= 0.0, "breedMultiplier can't be negative");
        if (recipeImage != null) {
            File recipeImageFile = new File(recipeImage);
            validator.validate(recipeImageFile.exists(), "recipeImage file could not be found");
            validator.validate(recipeImageFile.canRead(), "recipeImage file could not be read");
        }
        
        // Particles start
        validator.validate(ticksPerParticleCycle >= 1L, "ticksPerParticleCycle must be at least 1");
        validator.notNull(particle, "particle");
        validator.validate(numberOfParticles >= 1, "numberOfParticles must be at least 1");
        validator.validate(particleCount >= 1, "particleCount must be at least 1");
        // Particles end
    }
}
