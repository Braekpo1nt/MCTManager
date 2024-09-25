package org.braekpo1nt.mctmanager.config.dto.org.bukkit.inventory.recipes;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.bukkit.Material;
import org.bukkit.inventory.FurnaceRecipe;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.recipe.CookingBookCategory;
import org.jetbrains.annotations.Nullable;

@Data
@EqualsAndHashCode(callSuper = true)
public class FurnaceRecipeDTO extends RecipeDTO {
    
    protected RecipeMaterial ingredient;
    protected float experience;
    protected int cookingtime;
    protected @Nullable CookingBookCategory category;
    
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
        if (category != null) {
            recipe.setCategory(category);
        }
        return recipe;
    }
    
}
