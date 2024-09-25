package org.braekpo1nt.mctmanager.config;

import org.braekpo1nt.mctmanager.config.dto.org.bukkit.inventory.recipes.RecipeDTO;
import org.braekpo1nt.mctmanager.config.dto.org.bukkit.inventory.recipes.RecipeChoiceDTO;
import org.braekpo1nt.mctmanager.config.dto.org.bukkit.inventory.recipes.ShapelessRecipeDTO;
import org.bukkit.Material;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

class ConfigUtilsTest {
    
    @Test
    void shapeless() {
        RecipeDTO actual = ConfigUtils.GSON.fromJson("""
{
    "type": "minecraft:crafting_shapeless",
    "namespacedKey": {
        "namespace": "mct",
        "key": "example"
    },
    "ingredients": [
        {
            "item": "minecraft:stone"
        },
        [
            {
                "item": "minecraft:cut_red_sandstone"
            }
        ]
    ],
    "result": {
        "id": "minecraft:stone"
    }
}
                """, RecipeDTO.class);
        List<RecipeChoiceDTO> expected = List.of(
                new RecipeChoiceDTO.MaterialChoiceDTO(Material.STONE),
                new RecipeChoiceDTO.MultipleMaterialChoiceDTO(Collections.singletonList(new RecipeChoiceDTO.MaterialChoiceDTO(Material.CUT_RED_SANDSTONE)))
        );
        Assertions.assertEquals(expected, ((ShapelessRecipeDTO) actual).getIngredients());
    }
    
}