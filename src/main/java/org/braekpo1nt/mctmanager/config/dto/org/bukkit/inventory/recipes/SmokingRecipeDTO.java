package org.braekpo1nt.mctmanager.config.dto.org.bukkit.inventory.recipes;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.SmokingRecipe;

@Data
@EqualsAndHashCode(callSuper = true)
public class SmokingRecipeDTO extends FurnaceRecipeDTO {
    
    @Override
    public Recipe toRecipe() {
        SmokingRecipe recipe = new SmokingRecipe(
                namespacedKey.toNamespacedKey(),
                result.toItemStack(),
                ingredient.toRecipeChoice(),
                experience,
                cookingtime
        );
        if (group != null) {
            recipe.setGroup(group);
        }
        if (category != null) {
            recipe.setCategory(category);
        }
        return recipe;
    }
    
}
