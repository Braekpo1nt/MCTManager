package org.braekpo1nt.mctmanager.config.dto.org.bukkit.inventory.recipes;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.braekpo1nt.mctmanager.config.dto.org.bukkit.inventory.ItemStackDTO;
import org.bukkit.inventory.Recipe;

import java.util.List;
import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = true)
public class ShapedRecipeDTO extends RecipeDTO {
    
    private List<String> pattern;
    private Map<String, ItemStackDTO> key;
    
    @Override
    public Recipe toRecipe() {
        return null;
    }
}
