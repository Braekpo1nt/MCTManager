package org.braekpo1nt.mctmanager.config.dto.org.bukkit.inventory.recipes;

import lombok.Data;
import org.braekpo1nt.mctmanager.config.dto.org.bukkit.inventory.ItemStackDTO;
import org.braekpo1nt.mctmanager.config.validation.Validatable;
import org.braekpo1nt.mctmanager.config.validation.Validator;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@Data
public abstract class RecipeDTO implements Validatable {
    
    protected NamespacedKey namespacedKey;
    protected String type;
    /**
     * The result of the craft. If left blank, the result will be {@link org.bukkit.Material#AIR}
     * Can be overridden in certain circumstances, such as crafting a game-specific item with
     * a customizable recipe. 
     */
    protected @Nullable ItemStackDTO result;
    protected @Nullable String group;
    
    public Recipe toRecipe() {
        if (result == null) {
            return toRecipe(new ItemStack(Material.AIR));
        }
        return toRecipe(result.toItemStack());
    }
    public abstract Recipe toRecipe(ItemStack result);
    
    public static List<Recipe> toRecipes(@NotNull List<RecipeDTO> recipeDTOS) {
        return recipeDTOS.stream().map(RecipeDTO::toRecipe).toList();
    }
    
    /**
     * @param recipeDTOS the RecipDTOs to collect the {@link NamespacedKey}s of
     * @return a list containing just the {@link #getNamespacedKey()}s of all given {@link RecipeDTO}s
     */
    public static List<NamespacedKey> toNamespacedKeys(@NotNull List<RecipeDTO> recipeDTOS) {
        return recipeDTOS.stream().map(RecipeDTO::getNamespacedKey).toList();
    }
    
    @Override
    public void validate(@NotNull Validator validator) {
        validator.notNull(namespacedKey, "namespacedKey can't be null");
        if (result != null) {
            result.validate(validator.path("result"));
        }
    }
}
