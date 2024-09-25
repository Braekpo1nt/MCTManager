package org.braekpo1nt.mctmanager.config.dto.org.bukkit.inventory.recipes;

import com.google.gson.*;

import java.lang.reflect.Type;

public class SmithingRecipeDTODeserializer implements JsonDeserializer<SmithingRecipeDTO> {
    @Override
    public SmithingRecipeDTO deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject jsonObject = json.getAsJsonObject();
        JsonElement typeElement = jsonObject.get("type");
        if (typeElement == null) {
            return null;
        }
        String type = typeElement.getAsString();
        switch (type) {
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
