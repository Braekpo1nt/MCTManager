package org.braekpo1nt.mctmanager.config.dto.org.bukkit.inventory.recipes;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.recipe.CraftingBookCategory;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = true)
public class ShapedRecipeDTO extends RecipeDTO {
    
    protected List<String> pattern;
    protected Map<Character, RecipeChoiceDTO> key;
    protected @Nullable CraftingBookCategory category;
    
    @Override
    public Recipe toRecipe() {
        ShapedRecipe recipe = new ShapedRecipe(namespacedKey, result.toItemStack());
        recipe.shape(pattern.toArray(new String[0]));
        for (Map.Entry<Character, RecipeChoiceDTO> entry : key.entrySet()) {
            char keyChar = entry.getKey();
            RecipeChoice item = entry.getValue().toRecipeChoice();
            recipe.setIngredient(keyChar, item);
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
