package org.braekpo1nt.mctmanager.config.dto.org.bukkit.inventory.recipes;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.braekpo1nt.mctmanager.config.dto.org.bukkit.inventory.ItemStackDTO;
import org.bukkit.inventory.Recipe;

@Data
@EqualsAndHashCode(callSuper = true)
public class FurnaceRecipeDTO extends RecipeDTO {
    
    private ItemStackDTO ingredient;
    private double experience;
    private long cookingtime;
    
    @Override
    public Recipe toRecipe() {
        return null;
    }
    
}
