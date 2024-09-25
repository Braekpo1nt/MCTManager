package org.braekpo1nt.mctmanager.config.dto.org.bukkit.inventory.recipes;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.bukkit.Material;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapelessRecipe;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class ShapelessRecipeDTO extends RecipeDTO {
    
    private List<RecipeMaterial> ingredients;
    
    @Override
    public Recipe toRecipe() {
        ShapelessRecipe recipe = new ShapelessRecipe(namespacedKey.toNamespacedKey(), result.toItemStack());
        for (RecipeMaterial ingredient : ingredients) {
            recipe.addIngredient(ingredient.getItem());
        }
        if (group != null) {
            recipe.setGroup(group);
        }
        return recipe;
    }
    
}
