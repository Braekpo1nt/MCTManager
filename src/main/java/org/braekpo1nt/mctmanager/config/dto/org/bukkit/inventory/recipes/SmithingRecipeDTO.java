package org.braekpo1nt.mctmanager.config.dto.org.bukkit.inventory.recipes;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.braekpo1nt.mctmanager.config.dto.org.bukkit.inventory.ItemStackDTO;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.SmithingRecipe;

@Data
@EqualsAndHashCode(callSuper = true)
public abstract class SmithingRecipeDTO extends RecipeDTO {
    
    protected RecipeMaterial template;
    protected RecipeMaterial base;
    protected RecipeMaterial addition;
    protected boolean copyDataComponents = false;
    
}
