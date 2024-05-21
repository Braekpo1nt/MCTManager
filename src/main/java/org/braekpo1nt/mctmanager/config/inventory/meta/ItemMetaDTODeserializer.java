package org.braekpo1nt.mctmanager.config.inventory.meta;

import com.google.gson.*;
import org.braekpo1nt.mctmanager.Main;

import java.lang.reflect.Type;

/**
 * Responsible for deserializing ItemMetaDTO objects and its child classes
 */
public class ItemMetaDTODeserializer implements JsonDeserializer<ItemMetaDTO> {
    @Override
    public ItemMetaDTO deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        
        JsonObject jsonObject = json.getAsJsonObject();
        JsonElement basePotionData = jsonObject.get("basePotionData");
        if (basePotionData != null) {
            return context.deserialize(json, PotionMetaDTO.class);
        }
        return Main.GSON.fromJson(json, ItemMetaDTO.class);
    }
}
