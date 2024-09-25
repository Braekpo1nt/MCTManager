package org.braekpo1nt.mctmanager.config.dto.org.bukkit.inventory.recipes;

import lombok.Data;
import org.braekpo1nt.mctmanager.config.dto.org.bukkit.NamespacedKeyDTO;
import org.braekpo1nt.mctmanager.config.dto.org.bukkit.inventory.ItemStackDTO;
import org.bukkit.inventory.Recipe;
import org.jetbrains.annotations.Nullable;

@Data
public abstract class RecipeDTO {
    
    protected NamespacedKeyDTO namespacedKey;
    protected String type;
    protected ItemStackDTO result;
    protected @Nullable String group;
    
    public abstract Recipe toRecipe();
    
}
