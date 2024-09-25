package org.braekpo1nt.mctmanager.config.dto.org.bukkit.inventory.recipes;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bukkit.Material;
import org.bukkit.inventory.RecipeChoice;

import java.util.List;

public interface RecipeChoiceDTO {
    RecipeChoice toRecipeChoice();
    
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    class MultipleMaterialChoiceDTO implements RecipeChoiceDTO {
        
        private List<RecipeChoiceDTO.MaterialChoiceDTO> items;
        
        @Override
        public RecipeChoice toRecipeChoice() {
            return new RecipeChoice.MaterialChoice(items.stream().map(RecipeChoiceDTO.MaterialChoiceDTO::getItem).toList());
        }
    }
    
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    class MaterialChoiceDTO implements RecipeChoiceDTO {
        
        private Material item;
        
        @Override
        public RecipeChoice toRecipeChoice() {
            return new RecipeChoice.MaterialChoice(item);
        }
    }
    
}
