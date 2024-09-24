package org.braekpo1nt.mctmanager.config.dto.org.bukkit.inventory.recipes;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.bukkit.Material;
import org.bukkit.inventory.FurnaceRecipe;
import org.bukkit.inventory.Recipe;

@Data
@EqualsAndHashCode(callSuper = true)
public class FurnaceRecipeDTO extends RecipeDTO {
    
    protected Material ingredient;
    protected float experience;
    protected int cookingtime;
    
    @Override
    public Recipe toRecipe() {
        return new FurnaceRecipe(
                namespacedKey.toNamespacedKey(), 
                result.toItemStack(), 
                ingredient, 
                experience, 
                cookingtime
        );
    }
    
}
