package org.braekpo1nt.mctmanager.config.dto.org.bukkit.inventory.recipes;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.bukkit.Material;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;

import java.util.List;
import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = true)
public class ShapedRecipeDTO extends RecipeDTO {
    
    protected List<String> pattern;
    protected Map<Character, Material> key;
    
    @Override
    public Recipe toRecipe() {
        ShapedRecipe recipe = new ShapedRecipe(namespacedKey.toNamespacedKey(), result.toItemStack());
        recipe.shape(pattern.toArray(new String[0]));
        for (Map.Entry<Character, Material> entry : key.entrySet()) {
            char keyChar = entry.getKey();
            Material item = entry.getValue();
            recipe.setIngredient(keyChar, item);
        }
        return recipe;
    }
}
