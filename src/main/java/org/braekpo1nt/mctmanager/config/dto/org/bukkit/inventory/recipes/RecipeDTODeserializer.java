package org.braekpo1nt.mctmanager.config.dto.org.bukkit.inventory.recipes;

import com.google.gson.*;

import java.lang.reflect.Type;

public class RecipeDTODeserializer implements JsonDeserializer<RecipeDTO> {
    
    @Override
    public RecipeDTO deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject jsonObject = json.getAsJsonObject();
        JsonElement typeElement = jsonObject.get("type");
        if (typeElement == null) {
            return null;
        }
        String type = typeElement.getAsString();
        switch (type) {
            case "minecraft:crafting_shaped" -> {
                return context.deserialize(json, ShapedRecipeDTO.class);
            }
            case "minecraft:crafting_shapeless" -> {
                return context.deserialize(json, ShapelessRecipeDTO.class);
            }
            case "minecraft:smelting" -> {
                return context.deserialize(json, FurnaceRecipeDTO.class);
            }
            case "minecraft:blasting" -> {
                return context.deserialize(json, BlastingRecipeDTO.class);
            }
            case "minecraft:campfire_cooking" -> {
                return context.deserialize(json, CampfireRecipeDTO.class);
            }
            case "minecraft:smoking" -> {
                return context.deserialize(json, SmokingRecipeDTO.class);
            }
            case "minecraft:stonecutting" -> {
                return context.deserialize(json, StonecuttingRecipeDTO.class);
            }
            case "minecraft:smithing_transform" -> {
                return context.deserialize(json, SmithingTransformRecipeDTO.class);
            }
            case "minecraft:smithing_trim" -> {
                return context.deserialize(json, SmithingTrimRecipeDTO.class);
            }
        }
        return null;
    }
}
