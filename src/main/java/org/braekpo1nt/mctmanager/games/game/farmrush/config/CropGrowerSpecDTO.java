package org.braekpo1nt.mctmanager.games.game.farmrush.config;

import lombok.Data;
import org.braekpo1nt.mctmanager.config.dto.org.bukkit.inventory.recipes.RecipeDTO;
import org.braekpo1nt.mctmanager.config.validation.Validatable;
import org.braekpo1nt.mctmanager.config.validation.Validator;
import org.braekpo1nt.mctmanager.games.game.farmrush.powerups.PowerupManager;
import org.braekpo1nt.mctmanager.games.game.farmrush.powerups.PowerupType;
import org.braekpo1nt.mctmanager.games.game.farmrush.powerups.specs.CropGrowerSpec;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

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
    
    public CropGrowerSpec toSpec() {
        ItemStack cropGrowerItem = PowerupManager.cropGrowerItem;
        cropGrowerItem.editMeta(meta -> meta.setCustomModelData(customModelData));
        return CropGrowerSpec.builder()
                .recipe(recipe.toRecipe(cropGrowerItem))
                .recipeKey(recipe.getNamespacedKey())
                .radius(radius)
                .ticksPerCycle(ticksPerCycle)
                .growthChance(growthChance)
                .build();
    }
    
    @Override
    public void validate(@NotNull Validator validator) {
        validator.notNull(recipe, "recipe");
        validator.validate(radius >= 0.0, "radius can't be negative");
        validator.validate(ticksPerCycle >= 0, "ticksPerCycle can't be negative");
        validator.validate(0 <= growthChance && growthChance <= 1.0, "growthChance must be between 0.0 and 1.0 inclusive");
    }
}
