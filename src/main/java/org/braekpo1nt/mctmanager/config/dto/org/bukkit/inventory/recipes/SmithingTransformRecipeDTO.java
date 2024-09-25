package org.braekpo1nt.mctmanager.config.dto.org.bukkit.inventory.recipes;

import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.SmithingTransformRecipe;

public class SmithingTransformRecipeDTO extends SmithingRecipeDTO {
    
    @Override
    public Recipe toRecipe() {
        return new SmithingTransformRecipe(
                namespacedKey.toNamespacedKey(),
                result.toItemStack(),
                template.toRecipeChoice(),
                base.toRecipeChoice(),
                addition.toRecipeChoice(),
                copyDataComponents
        );
    }
}
