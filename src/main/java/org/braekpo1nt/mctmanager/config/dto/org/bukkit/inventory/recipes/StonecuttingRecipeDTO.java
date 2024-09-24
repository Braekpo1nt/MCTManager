package org.braekpo1nt.mctmanager.config.dto.org.bukkit.inventory.recipes;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.braekpo1nt.mctmanager.config.dto.org.bukkit.inventory.ItemStackDTO;
import org.bukkit.Material;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.StonecuttingRecipe;

@Data
@EqualsAndHashCode(callSuper = true)
public class StonecuttingRecipeDTO extends RecipeDTO {
    
    private Material ingredient;
    
    @Override
    public Recipe toRecipe() {
        return new StonecuttingRecipe(namespacedKey.toNamespacedKey(), result.toItemStack(), ingredient);
    }
    
}
