package org.braekpo1nt.mctmanager.config.dto.org.bukkit.inventory.recipes;


import lombok.Data;
import lombok.EqualsAndHashCode;
import org.bukkit.inventory.CampfireRecipe;
import org.bukkit.inventory.Recipe;

@Data
@EqualsAndHashCode(callSuper = true)
public class CampfireRecipeDTO extends FurnaceRecipeDTO {
    
    @Override
    public Recipe toRecipe() {
        CampfireRecipe recipe = new CampfireRecipe(
                namespacedKey.toNamespacedKey(),
                result.toItemStack(),
                ingredient.getItem(),
                experience,
                cookingtime
        );
        if (group != null) {
            recipe.setGroup(group);
        }
        return recipe;
    }
    
}
