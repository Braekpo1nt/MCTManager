package org.braekpo1nt.mctmanager.config;

import org.braekpo1nt.mctmanager.config.dto.org.bukkit.NamespacedKeyDTO;
import org.braekpo1nt.mctmanager.config.dto.org.bukkit.inventory.recipes.RecipeDTO;
import org.braekpo1nt.mctmanager.config.dto.org.bukkit.inventory.recipes.RecipeChoiceDTO;
import org.braekpo1nt.mctmanager.config.dto.org.bukkit.inventory.recipes.ShapelessRecipeDTO;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

class ConfigUtilsTest {
    
    @Test
    void stringTest() {
        NamespacedKeyDTO actual = ConfigUtils.GSON.fromJson("""
"minecraft:stone"
                """, NamespacedKeyDTO.class);
        Assertions.assertEquals("minecraft", actual.getNamespace());
        Assertions.assertEquals("stone", actual.getKey());
    }
    
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
                new RecipeChoiceDTO.Single(Material.STONE, null),
                new RecipeChoiceDTO.Multiple(Collections.singletonList(new RecipeChoiceDTO.Single(Material.CUT_RED_SANDSTONE, null)))
        );
        Assertions.assertEquals(expected, ((ShapelessRecipeDTO) actual).getIngredients());
        Assertions.assertInstanceOf(ShapelessRecipeDTO.class, actual);
    }
    
    @Test
    void tags() {
        RecipeDTO actual = ConfigUtils.GSON.fromJson("""
{
    "type": "minecraft:crafting_shapeless",
    "namespacedKey": {
        "namespace": "mct",
        "key": "example"
    },
    "ingredients": [
        {
            "item": "minecraft:stone",
            "tag": "minecraft:acacia_logs"
        },
        [
            {
                "tag": "minecraft:cherry_logs"
            }
        ]
    ],
    "result": {
        "id": "minecraft:stone"
    }
}
                """, RecipeDTO.class);
        List<RecipeChoiceDTO> expected = List.of(
                new RecipeChoiceDTO.Single(Material.STONE, new NamespacedKeyDTO("minecraft", "acacia_logs")),
                new RecipeChoiceDTO.Multiple(Collections.singletonList(new RecipeChoiceDTO.Single(null, new NamespacedKeyDTO("minecraft", "cherry_logs"))))
        );
        Assertions.assertEquals(expected, ((ShapelessRecipeDTO) actual).getIngredients());
        Assertions.assertInstanceOf(ShapelessRecipeDTO.class, actual);
    }
    
}