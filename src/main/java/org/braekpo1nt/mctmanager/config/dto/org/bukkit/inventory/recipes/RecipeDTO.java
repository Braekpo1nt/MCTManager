package org.braekpo1nt.mctmanager.config.dto.org.bukkit.inventory.recipes;

import lombok.Data;
import org.braekpo1nt.mctmanager.config.dto.org.bukkit.NamespacedKeyDTO;
import org.braekpo1nt.mctmanager.config.dto.org.bukkit.inventory.ItemStackDTO;
import org.bukkit.inventory.Recipe;

@Data
public abstract class RecipeDTO {
    
    protected String type;
    protected ItemStackDTO result;
    protected NamespacedKeyDTO namespacedKey;
    
    abstract Recipe toRecipe();
    
}
