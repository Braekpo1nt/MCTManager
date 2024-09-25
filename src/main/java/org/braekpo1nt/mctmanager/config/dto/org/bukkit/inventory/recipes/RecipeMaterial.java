package org.braekpo1nt.mctmanager.config.dto.org.bukkit.inventory.recipes;

import lombok.Data;
import org.bukkit.Material;
import org.bukkit.inventory.RecipeChoice;

import java.util.List;

@Data
public class RecipeMaterial {
    private Material item;
    
    public static List<Material> toMaterials(List<RecipeMaterial> recipeMaterials) {
        return recipeMaterials.stream().map(RecipeMaterial::getItem).toList();
    }
    
    public static RecipeChoice toRecipeChoice(List<RecipeMaterial> recipeMaterials) {
        return new RecipeChoice.MaterialChoice(toMaterials(recipeMaterials));
    }
}
