package org.braekpo1nt.mctmanager.config.dto.org.bukkit.inventory.recipes;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.StonecuttingRecipe;

@Data
@EqualsAndHashCode(callSuper = true)
public class StonecuttingRecipeDTO extends RecipeDTO {
    
    private Material ingredient;
    
    @Override
    public Recipe toRecipe(ItemStack result) {
        StonecuttingRecipe recipe = new StonecuttingRecipe(namespacedKey, result, ingredient);
        if (group != null) {
            recipe.setGroup(group);
        }
        return recipe;
    }
    
}
