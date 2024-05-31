package org.braekpo1nt.mctmanager.config.dto.org.bukkit.inventory.meta;

import com.google.gson.*;
import org.braekpo1nt.mctmanager.config.ConfigUtils;

import java.lang.reflect.Type;

/**
 * Responsible for deserializing ItemMetaDTO interface implementations
 */
public class ItemMetaDTODeserializer implements JsonDeserializer<ItemMetaDTO> {
    @Override
    public ItemMetaDTO deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        
        JsonObject jsonObject = json.getAsJsonObject();
        JsonElement basePotionData = jsonObject.get("basePotionData");
        if (basePotionData != null) {
            return context.deserialize(json, PotionMetaDTO.class);
        }
        return ConfigUtils.GSON.fromJson(json, ItemMetaDTOImpl.class);
    }
    
}
