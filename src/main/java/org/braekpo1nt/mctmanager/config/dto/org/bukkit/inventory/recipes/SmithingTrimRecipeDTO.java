package org.braekpo1nt.mctmanager.config.dto.org.bukkit.inventory.recipes;

import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.SmithingTrimRecipe;

public class SmithingTrimRecipeDTO extends SmithingRecipeDTO {
    
    @Override
    public Recipe toRecipe() {
        return new SmithingTrimRecipe(
                namespacedKey,
                template.toRecipeChoice(),
                base.toRecipeChoice(),
                addition.toRecipeChoice(),
                copyDataComponents
        );
    }
}
