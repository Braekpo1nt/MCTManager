package org.braekpo1nt.mctmanager.config.dto.net.kyori.adventure.text;

import com.google.gson.*;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;

import java.lang.reflect.Type;

public class ComponentAdapter implements JsonSerializer<Component>, JsonDeserializer<Component> {
    
    @Override
    public JsonElement serialize(Component src, Type typeOfSrc, JsonSerializationContext context) {
        if (src == null) {
            return null;
        }
        return GsonComponentSerializer.gson().serializeToTree(src);
    }
    
    @Override
    public Component deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        if (json == null) {
            return null;
        }
        return GsonComponentSerializer.gson().deserializeFromTree(json);
    }
}
