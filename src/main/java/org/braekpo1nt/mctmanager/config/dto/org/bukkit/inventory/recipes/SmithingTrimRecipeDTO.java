package org.braekpo1nt.mctmanager.config.dto.org.bukkit.inventory.recipes;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.braekpo1nt.mctmanager.config.validation.Validator;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.SmithingTrimRecipe;
import org.bukkit.inventory.meta.trim.TrimPattern;
import org.jetbrains.annotations.NotNull;

@Data
@EqualsAndHashCode(callSuper = true)
public class SmithingTrimRecipeDTO extends SmithingRecipeDTO {
    
    private final TrimPattern trimPattern;
    
    @Override
    public void validate(@NotNull Validator validator) {
        super.validate(validator);
        validator.validate(template.toRecipeChoice() != RecipeChoice.empty(), "template can't be empty");
        validator.validate(base.toRecipeChoice() != RecipeChoice.empty(), "base can't be empty");
        validator.validate(addition.toRecipeChoice() != RecipeChoice.empty(), "addition can't be empty");
    }
    
    @Override
    public Recipe toRecipe(ItemStack result) {
        return new SmithingTrimRecipe(
                namespacedKey,
                template.toRecipeChoice(),
                base.toRecipeChoice(),
                addition.toRecipeChoice(),
                trimPattern,
                copyDataComponents
        );
    }
}
