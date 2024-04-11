package org.braekpo1nt.mctmanager.games.game.config.inventory.meta;

import com.google.gson.*;

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
        return new Gson().fromJson(json, ItemMetaDTO.class);
    }
}
