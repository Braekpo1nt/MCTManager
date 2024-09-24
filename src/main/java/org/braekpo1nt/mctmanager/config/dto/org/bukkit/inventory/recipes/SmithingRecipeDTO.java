package org.braekpo1nt.mctmanager.config.dto.org.bukkit.inventory.recipes;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.braekpo1nt.mctmanager.config.dto.org.bukkit.inventory.ItemStackDTO;
import org.bukkit.inventory.Recipe;

@Data
@EqualsAndHashCode(callSuper = true)
public class SmithingRecipeDTO extends RecipeDTO {
    
    private ItemStackDTO template;
    private ItemStackDTO base;
    private ItemStackDTO addition;
    
    @Override
    public Recipe toRecipe() {
        return null;
    }
    
}
