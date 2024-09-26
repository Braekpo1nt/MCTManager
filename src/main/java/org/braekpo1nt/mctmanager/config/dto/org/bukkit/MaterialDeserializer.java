package org.braekpo1nt.mctmanager.config.dto.org.bukkit;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import org.bukkit.Material;

import java.lang.reflect.Type;

public class MaterialDeserializer implements JsonDeserializer<Material> {
    
    @Override
    public Material deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        String materialStr = json.getAsString();
        if (materialStr == null) {
            return null;
        }
        return Material.matchMaterial(materialStr);
    }
}
