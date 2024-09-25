package org.braekpo1nt.mctmanager.config.dto.org.bukkit.inventory.recipes;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bukkit.Material;
import org.bukkit.inventory.RecipeChoice;

import java.util.List;

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
            return new RecipeChoice.MaterialChoice(items.stream().map(Single::getItem).toList());
        }
    }
    
    /**
     * Represents a single choice of item
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    class Single implements RecipeChoiceDTO {
        
        private Material item;
        
        @Override
        public RecipeChoice toRecipeChoice() {
            return new RecipeChoice.MaterialChoice(item);
        }
    }
    
}
