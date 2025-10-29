package org.braekpo1nt.mctmanager.config.dto.org.bukkit.util;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import org.bukkit.util.BoundingBox;

import java.lang.reflect.Type;

/**
 * Ensures that {@link BoundingBox}es are deserialized correctly, with min and max values set
 * appropriately.
 */
public class BoundingBoxDeserializer implements JsonDeserializer<BoundingBox> {
    @Override
    public BoundingBox deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        if (json == null) {
            return null;
        }
        JsonObject jsonObject = json.getAsJsonObject();
        double minX = jsonObject.get("minX").getAsDouble();
        double minY = jsonObject.get("minY").getAsDouble();
        double minZ = jsonObject.get("minZ").getAsDouble();
        double maxX = jsonObject.get("maxX").getAsDouble();
        double maxY = jsonObject.get("maxY").getAsDouble();
        double maxZ = jsonObject.get("maxZ").getAsDouble();
        return new BoundingBox(minX, minY, minZ, maxX, maxY, maxZ);
    }
}
