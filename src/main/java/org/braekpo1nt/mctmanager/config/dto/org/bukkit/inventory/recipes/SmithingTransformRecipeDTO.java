package org.braekpo1nt.mctmanager.config.dto.org.bukkit.inventory.recipes;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.SmithingTransformRecipe;

public class SmithingTransformRecipeDTO extends SmithingRecipeDTO {
    
    @Override
    public Recipe toRecipe(ItemStack result) {
        return new SmithingTransformRecipe(
                namespacedKey,
                result,
                template.toRecipeChoice(),
                base.toRecipeChoice(),
                addition.toRecipeChoice(),
                copyDataComponents
        );
    }
}
