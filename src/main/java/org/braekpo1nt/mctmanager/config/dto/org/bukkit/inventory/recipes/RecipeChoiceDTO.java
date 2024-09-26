package org.braekpo1nt.mctmanager.config.dto.org.bukkit.inventory.recipes;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.braekpo1nt.mctmanager.config.ConfigUtils;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Tag;
import org.bukkit.inventory.RecipeChoice;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public interface RecipeChoiceDTO {
    RecipeChoice toRecipeChoice();
    
    /**
     * Represents multiple choices of item 
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    class Multiple implements RecipeChoiceDTO {
        
        private List<Single> items;
        
        @Override
        public RecipeChoice toRecipeChoice() {
            List<Material> materialList = items.stream().flatMap(single -> single.toMaterialList().stream()).toList();
            if (materialList.isEmpty()) {
                return RecipeChoice.empty();
            }
            return new RecipeChoice.MaterialChoice(materialList);
        }
    }
    
    /**
     * Represents a single choice of item or tag.
     * If an item and a tag are provided, all 
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    class Single implements RecipeChoiceDTO {
        
        private @Nullable Material item;
        private @Nullable NamespacedKey tag;
        
        public List<Material> toMaterialList() {
            if (item == null && tag == null) {
                return Collections.emptyList();
            }
            Set<Material> items = new HashSet<>();
            if (tag != null) {
                Tag<Material> materialTag = ConfigUtils.toTag(tag);
                if (materialTag != null) {
                    items.addAll(materialTag.getValues());
                }
            }
            if (item != null) {
                items.add(item);
            }
            return new ArrayList<>(items);
        }
        
        @Override
        public RecipeChoice toRecipeChoice() {
            List<Material> materialList = this.toMaterialList();
            if (materialList.isEmpty()) {
                return RecipeChoice.empty();
            }
            return new RecipeChoice.MaterialChoice(materialList);
        }
    }
    
}
