package org.braekpo1nt.mctmanager.config;

import com.google.gson.JsonParseException;
import org.braekpo1nt.mctmanager.config.dto.org.bukkit.inventory.recipes.RecipeChoiceDTO;
import org.braekpo1nt.mctmanager.config.dto.org.bukkit.inventory.recipes.RecipeDTO;
import org.braekpo1nt.mctmanager.config.dto.org.bukkit.inventory.recipes.ShapelessRecipeDTO;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

class ConfigUtilsTest {
    
    @Test
    void namespacedKey() {
        NamespacedKey actual1 = ConfigUtils.GSON.fromJson("""
                "minecraft:stone"
                                """, NamespacedKey.class);
        Assertions.assertEquals(NamespacedKey.minecraft("stone"), actual1);
        
        NamespacedKey actual2 = ConfigUtils.GSON.fromJson("""
                {
                    "namespace": "minecraft",
                    "key": "stone"
                }
                                """, NamespacedKey.class);
        Assertions.assertEquals(NamespacedKey.minecraft("stone"), actual2);
        
        NamespacedKey actual3 = ConfigUtils.GSON.fromJson("""
                "stone"
                                """, NamespacedKey.class);
        Assertions.assertEquals(NamespacedKey.minecraft("stone"), actual3);
        
        NamespacedKey actual4 = ConfigUtils.GSON.fromJson("""
                {
                    "key": "stone"
                }
                                """, NamespacedKey.class);
        Assertions.assertEquals(NamespacedKey.minecraft("stone"), actual4);
        
        Assertions.assertEquals(NamespacedKey.minecraft("minecraftstone"), ConfigUtils.GSON.fromJson("\"minecraftstone\"", NamespacedKey.class));
        Assertions.assertThrows(JsonParseException.class, () -> ConfigUtils.GSON.fromJson("\"minecrafT:stone\"", NamespacedKey.class));
    }
    
    @Test
    void missingNamespacedKey() {
        Assertions.assertThrows(JsonParseException.class, () -> ConfigUtils.GSON.fromJson("""
                {
                    "type": "minecraft:crafting_shapeless",
                    "ingredients": [
                        {
                            "item": "minecraft:stone"
                        }
                    ],
                    "result": {
                        "id": "minecraft:stone"
                    }
                }
                                """, RecipeDTO.class));
    }
    
    @Test
    void shapeless() {
        RecipeDTO actual = ConfigUtils.GSON.fromJson("""
                {
                    "type": "minecraft:crafting_shapeless",
                    "namespacedKey": "mct:example",
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
                    "namespacedKey": "mct:example",
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
                new RecipeChoiceDTO.Single(Material.STONE, new NamespacedKey("minecraft", "acacia_logs")),
                new RecipeChoiceDTO.Multiple(Collections.singletonList(new RecipeChoiceDTO.Single(null, new NamespacedKey("minecraft", "cherry_logs"))))
        );
        Assertions.assertEquals(expected, ((ShapelessRecipeDTO) actual).getIngredients());
        Assertions.assertInstanceOf(ShapelessRecipeDTO.class, actual);
    }
    
}