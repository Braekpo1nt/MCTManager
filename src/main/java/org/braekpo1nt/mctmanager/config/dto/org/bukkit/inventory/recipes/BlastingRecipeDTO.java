package org.braekpo1nt.mctmanager.config.dto.org.bukkit.inventory.recipes;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.bukkit.inventory.BlastingRecipe;
import org.bukkit.inventory.Recipe;

@Data
@EqualsAndHashCode(callSuper = true)
public class BlastingRecipeDTO extends FurnaceRecipeDTO {
    
    @Override
    public Recipe toRecipe() {
        return new BlastingRecipe(
                namespacedKey.toNamespacedKey(),
                result.toItemStack(),
                ingredient,
                experience,
                cookingtime
        );
    }
    
}
