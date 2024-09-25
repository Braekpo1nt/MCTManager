package org.braekpo1nt.mctmanager.config.dto.org.bukkit.inventory.recipes;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.recipe.CraftingBookCategory;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class ShapelessRecipeDTO extends RecipeDTO {
    
    protected List<RecipeChoiceDTO> ingredients;
    protected @Nullable CraftingBookCategory category;
    
    @Override
    public Recipe toRecipe() {
        ShapelessRecipe recipe = new ShapelessRecipe(namespacedKey.toNamespacedKey(), result.toItemStack());
        for (RecipeChoiceDTO recipeChoiceDTO : ingredients) {
            recipe.addIngredient(recipeChoiceDTO.toRecipeChoice());
        }
        if (group != null) {
            recipe.setGroup(group);
        }
        if (category != null) {
            recipe.setCategory(category);
        }
        return recipe;
    }
    
}
