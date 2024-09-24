package org.braekpo1nt.mctmanager.config.dto.org.bukkit.inventory.recipes;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.braekpo1nt.mctmanager.config.dto.org.bukkit.inventory.ItemStackDTO;
import org.bukkit.inventory.Recipe;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class ShapelessRecipeDTO extends RecipeDTO {
    
    private List<ItemStackDTO> ingredients;
    
    @Override
    public Recipe toRecipe() {
        return null;
    }
    
}
