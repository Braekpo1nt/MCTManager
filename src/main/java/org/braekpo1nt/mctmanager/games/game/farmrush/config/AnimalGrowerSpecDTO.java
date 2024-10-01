package org.braekpo1nt.mctmanager.games.game.farmrush.config;

import lombok.Data;
import org.braekpo1nt.mctmanager.config.dto.org.bukkit.inventory.recipes.RecipeDTO;
import org.braekpo1nt.mctmanager.config.validation.Validatable;
import org.braekpo1nt.mctmanager.config.validation.Validator;
import org.braekpo1nt.mctmanager.games.game.farmrush.powerups.PowerupManager;
import org.braekpo1nt.mctmanager.games.game.farmrush.powerups.PowerupType;
import org.braekpo1nt.mctmanager.games.game.farmrush.powerups.specs.AnimalGrowerSpec;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

@Data
public class AnimalGrowerSpecDTO implements Validatable {
    
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
     * How many seconds pass between checks for new entities in the radius
     */
    private int seconds = 0;
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
    
    public AnimalGrowerSpec toSpec() {
        ItemStack powerupItem = PowerupManager.animalGrowerItem;
        powerupItem.editMeta(meta -> meta.setCustomModelData(customModelData));
        return AnimalGrowerSpec.builder()
                .recipe(recipe.toRecipe(powerupItem))
                .recipeKey(recipe.getNamespacedKey())
                .seconds(seconds)
                .radius(radius)
                .ageMultiplier(ageMultiplier)
                .breedMultiplier(breedMultiplier)
                .build();
    }
    
    @Override
    public void validate(@NotNull Validator validator) {
        validator.notNull(recipe, "recipe");
        validator.validate(radius >= 0.0, "radius can't be negative");
        validator.validate(seconds >= 0, "seconds can't be negative");
        validator.validate(ageMultiplier >= 0.0, "ageMultiplier can't be negative");
        validator.validate(breedMultiplier >= 0.0, "breedMultiplier can't be negative");
    }
}
