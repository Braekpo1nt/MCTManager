package org.braekpo1nt.mctmanager.config.dto.org.bukkit.inventory.meta;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import org.braekpo1nt.mctmanager.config.ConfigUtils;

import java.lang.reflect.Type;

/**
 * Responsible for deserializing ItemMetaDTO interface implementations
 */
public class ItemMetaDTODeserializer implements JsonDeserializer<ItemMetaDTO> {
    @Override
    public ItemMetaDTO deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject jsonObject = json.getAsJsonObject();
        JsonElement basePotionType = jsonObject.get("basePotionType");
        JsonElement fireworkPower = jsonObject.get("fireworkPower");
        if (basePotionType != null) {
            return context.deserialize(json, PotionMetaDTO.class);
        }
        
        if (fireworkPower != null) {
            return context.deserialize(json, FireworkMetaDTO.class);
        }
        
        return ConfigUtils.GSON.fromJson(json, ItemMetaDTOImpl.class);
    }
    
}
