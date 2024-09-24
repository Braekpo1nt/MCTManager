package org.braekpo1nt.mctmanager.config.dto.org.bukkit.inventory.recipes;

import lombok.Data;
import org.bukkit.inventory.Recipe;

@Data
public abstract class RecipeDTO {
    
    private String type;
    private RecipeResultDTO result;
    
    abstract Recipe toRecipe();
    
}
