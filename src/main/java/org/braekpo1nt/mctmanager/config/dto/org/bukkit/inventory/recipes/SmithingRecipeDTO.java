package org.braekpo1nt.mctmanager.config.dto.org.bukkit.inventory.recipes;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public abstract class SmithingRecipeDTO extends RecipeDTO {
    
    protected RecipeChoiceDTO template;
    protected RecipeChoiceDTO base;
    protected RecipeChoiceDTO addition;
    protected boolean copyDataComponents = false;
    
}
