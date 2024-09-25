package org.braekpo1nt.mctmanager.config.dto.org.bukkit.inventory.recipes;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.bukkit.Material;
import org.bukkit.inventory.FurnaceRecipe;
import org.bukkit.inventory.Recipe;

@Data
@EqualsAndHashCode(callSuper = true)
public class FurnaceRecipeDTO extends RecipeDTO {
    
    protected RecipeMaterial ingredient;
    protected float experience;
    protected int cookingtime;
    
    @Override
    public Recipe toRecipe() {
        FurnaceRecipe recipe = new FurnaceRecipe(
                namespacedKey.toNamespacedKey(),
                result.toItemStack(),
                ingredient.getItem(),
                experience,
                cookingtime
        );
        if (group != null) {
            recipe.setGroup(group);
        }
        return recipe;
    }
    
}
