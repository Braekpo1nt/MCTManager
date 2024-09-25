package org.braekpo1nt.mctmanager.config.dto.org.bukkit.inventory.recipes.typeadapters;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import org.braekpo1nt.mctmanager.config.dto.org.bukkit.inventory.recipes.RecipeMaterial;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Allows {@link List< RecipeMaterial >} to be interpreted as a singleton list if it is just a single
 * {@link RecipeMaterial} object, and a list otherwise.
 */
public class RecipeMaterialDeserializer implements JsonDeserializer<List<RecipeMaterial>> {
    @Override
    public List<RecipeMaterial> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        
        if (json.isJsonArray()) {
            List<RecipeMaterial> items = new ArrayList<>();
            for (JsonElement element : json.getAsJsonArray()) {
                items.add(context.deserialize(element, RecipeMaterial.class));
            }
            return items;
        } else if (json.isJsonObject()) {
            return List.of(context.deserialize(json, RecipeMaterial.class));
        }
        return Collections.emptyList();
    }
}
