package org.braekpo1nt.mctmanager.config.dto.org.bukkit.inventory.recipes.typeadapters;

import com.google.gson.*;
import org.braekpo1nt.mctmanager.config.dto.org.bukkit.inventory.recipes.RecipeChoiceDTO;

import java.lang.reflect.Type;
import java.util.List;

/**
 * Allows {@link List< RecipeChoiceDTO >} to be interpreted as a singleton list if it is just a single
 * {@link RecipeChoiceDTO} object, and a list otherwise.
 */
public class RecipeMaterialDeserializer implements JsonDeserializer<RecipeChoiceDTO> {
    @Override
    public RecipeChoiceDTO deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        
        if (json.isJsonObject()) {
            return context.deserialize(json, RecipeChoiceDTO.Single.class);
        } else if (json.isJsonArray()) {
            JsonObject multiple = new JsonObject();
            multiple.add("items", json);
            return context.deserialize(multiple, RecipeChoiceDTO.Multiple.class);
        }
        
        return null;
    }
}
