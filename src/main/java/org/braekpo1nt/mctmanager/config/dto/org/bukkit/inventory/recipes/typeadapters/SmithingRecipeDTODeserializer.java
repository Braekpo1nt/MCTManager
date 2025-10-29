package org.braekpo1nt.mctmanager.config.dto.org.bukkit.inventory.recipes.typeadapters;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import org.braekpo1nt.mctmanager.config.dto.org.bukkit.inventory.recipes.SmithingRecipeDTO;
import org.braekpo1nt.mctmanager.config.dto.org.bukkit.inventory.recipes.SmithingTransformRecipeDTO;
import org.braekpo1nt.mctmanager.config.dto.org.bukkit.inventory.recipes.SmithingTrimRecipeDTO;

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
