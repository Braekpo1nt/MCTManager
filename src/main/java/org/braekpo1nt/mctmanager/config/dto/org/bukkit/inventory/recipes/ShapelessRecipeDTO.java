package org.braekpo1nt.mctmanager.config.dto.org.bukkit.inventory.recipes;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.braekpo1nt.mctmanager.config.dto.org.bukkit.inventory.ItemStackDTO;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapelessRecipe;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class ShapelessRecipeDTO extends RecipeDTO {
    
    private List<ItemStackDTO> ingredients;
    
    @Override
    public Recipe toRecipe() {
        ShapelessRecipe recipe = new ShapelessRecipe(namespacedKey.toNamespacedKey(), result.toItemStack());
        for (ItemStackDTO ingredient : ingredients) {
            recipe.addIngredient(ingredient.toItemStack());
        }
        return recipe;
    }
    
}
