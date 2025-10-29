package org.braekpo1nt.mctmanager.config.dto.net.kyori.adventure.text;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.JsonSyntaxException;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;

import java.lang.reflect.Type;

public class ComponentAdapter implements JsonSerializer<Component>, JsonDeserializer<Component> {
    
    @Override
    public JsonElement serialize(Component src, Type typeOfSrc, JsonSerializationContext context) {
        if (src == null) {
            return null;
        }
        try {
            return GsonComponentSerializer.gson().serializeToTree(src);
        } catch (JsonIOException | JsonSyntaxException e) {
            throw new JsonSyntaxException(String.format("Could not serialize component, %s", src), e);
        }
    }
    
    @Override
    public Component deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        if (json == null) {
            return null;
        }
        try {
            return GsonComponentSerializer.gson().deserializeFromTree(json);
        } catch (JsonIOException | JsonSyntaxException e) {
            throw new JsonSyntaxException(String.format("Could not deserialize component from the given json string \"%s\"", json), e);
        }
    }
}
