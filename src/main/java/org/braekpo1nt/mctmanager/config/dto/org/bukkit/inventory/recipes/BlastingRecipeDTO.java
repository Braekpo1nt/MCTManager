package org.braekpo1nt.mctmanager.config.dto.org.bukkit.inventory.recipes;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.bukkit.inventory.BlastingRecipe;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;

@Data
@EqualsAndHashCode(callSuper = true)
public class BlastingRecipeDTO extends FurnaceRecipeDTO {
    
    @Override
    public Recipe toRecipe(ItemStack result) {
        BlastingRecipe recipe = new BlastingRecipe(
                namespacedKey,
                result,
                ingredient.toRecipeChoice(),
                experience,
                cookingtime
        );
        if (group != null) {
            recipe.setGroup(group);
        }
        if (category != null) {
            recipe.setCategory(category);
        }
        return recipe;
    }
    
}
