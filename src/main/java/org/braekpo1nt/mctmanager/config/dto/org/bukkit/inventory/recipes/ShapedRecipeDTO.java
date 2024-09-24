package org.braekpo1nt.mctmanager.config.dto.org.bukkit.inventory.recipes;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.braekpo1nt.mctmanager.config.dto.org.bukkit.inventory.ItemStackDTO;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;

import java.util.List;
import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = true)
public class ShapedRecipeDTO extends RecipeDTO {
    
    protected List<String> pattern;
    protected Map<Character, ItemStackDTO> key;
    
    @Override
    public Recipe toRecipe() {
        ShapedRecipe shapedRecipe = new ShapedRecipe(namespacedKey.toNamespacedKey(), result.toItemStack());
        shapedRecipe.shape(pattern.toArray(new String[0]));
        for (Map.Entry<Character, ItemStackDTO> entry : key.entrySet()) {
            char keyChar = entry.getKey();
            ItemStack item = entry.getValue().toItemStack();
            shapedRecipe.setIngredient(keyChar, item);
        }
        return shapedRecipe;
    }
}
