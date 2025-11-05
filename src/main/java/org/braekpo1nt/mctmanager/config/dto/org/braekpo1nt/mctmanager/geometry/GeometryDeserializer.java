package org.braekpo1nt.mctmanager.config.dto.org.braekpo1nt.mctmanager.geometry;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import org.braekpo1nt.mctmanager.geometry.BoundingCylinder;
import org.braekpo1nt.mctmanager.geometry.BoundingRectangle;
import org.braekpo1nt.mctmanager.geometry.Geometry;

import java.lang.reflect.Type;

public class GeometryDeserializer implements JsonDeserializer<Geometry> {
    @Override
    public Geometry deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject jsonObject = json.getAsJsonObject();
        JsonElement centerX = jsonObject.get("centerX");
        if (centerX != null) {
            return context.deserialize(json, BoundingCylinder.class);
        }
        JsonElement minX = jsonObject.get("minX");
        if (minX != null) {
            return context.deserialize(json, BoundingRectangle.class);
        }
        throw new JsonParseException("Unknown geometry object: " + jsonObject.getAsString());
    }
}
