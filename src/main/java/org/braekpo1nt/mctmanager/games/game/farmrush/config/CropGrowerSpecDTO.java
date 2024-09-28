package org.braekpo1nt.mctmanager.games.game.farmrush.config;

import lombok.Data;
import org.braekpo1nt.mctmanager.config.dto.org.bukkit.inventory.recipes.RecipeDTO;
import org.braekpo1nt.mctmanager.config.validation.Validatable;
import org.braekpo1nt.mctmanager.config.validation.Validator;
import org.braekpo1nt.mctmanager.games.game.farmrush.powerups.Powerup;
import org.braekpo1nt.mctmanager.games.game.farmrush.powerups.PowerupManager;
import org.braekpo1nt.mctmanager.games.game.farmrush.powerups.specs.CropGrowerSpec;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

@Data
class CropGrowerSpecDTO implements Validatable {
    private Powerup.Type type = Powerup.Type.CROP_GROWER;
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
     * How many seconds pass between probability checks
     * If this is {@code x}, then each block will have a {@link #growthChance}
     * chance to grow to the next cycle every {@code x} seconds.
     * Defaults to 0, can't be negative.
     */
    private int seconds = 0;
    /**
     * the chance per probability check for a crop to grow to the next age.
     * A probability check happens every {@link #seconds} seconds
     * Defaults to 1.0, must be between 0.0 and 1.0 inclusive.
     */
    private double growthChance = 1.0;
    
    public CropGrowerSpec toSpec() {
        ItemStack powerupItem = PowerupManager.typeToItem.get(type);
        powerupItem.editMeta(meta -> meta.setCustomModelData(customModelData));
        return CropGrowerSpec.builder()
                .type(type)
                .item(powerupItem)
                .recipe(recipe.toRecipe(powerupItem))
                .recipeKey(recipe.getNamespacedKey())
                .radius(radius)
                .count(seconds)
                .probability(growthChance)
                .build();
//        (type, recipe.toRecipe(result), recipe.getNamespacedKey(), radius);
    }
    
    @Override
    public void validate(@NotNull Validator validator) {
        validator.notNull(recipe, "recipe");
        validator.validate(radius >= 0.0, "radius can't be negative");
        validator.validate(seconds >= 0, "seconds can't be negative");
        validator.validate(0 <= growthChance && growthChance <= 1.0, "growthChance must be between 0.0 and 1.0 inclusive");
    }
}
