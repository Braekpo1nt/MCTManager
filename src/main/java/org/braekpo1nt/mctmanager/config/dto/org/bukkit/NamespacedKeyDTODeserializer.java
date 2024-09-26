package org.braekpo1nt.mctmanager.config.dto.org.bukkit;

import com.google.gson.*;

import java.lang.reflect.Type;

public class NamespacedKeyDTODeserializer implements JsonDeserializer<NamespacedKeyDTO> {
    @Override
    public NamespacedKeyDTO deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        if (json.isJsonPrimitive()) {
            String namespacedKeyStr = json.getAsString();
            if (namespacedKeyStr == null) {
                return null;
            }
            int indexOfColon = namespacedKeyStr.indexOf(':');
            if (indexOfColon < 0) {
                return null;
            }
            String namespace = namespacedKeyStr.substring(0, indexOfColon);
            String key = namespacedKeyStr.substring(indexOfColon+1);
            return new NamespacedKeyDTO(namespace, key);
        } else if (json.isJsonObject()) {
            JsonObject jsonObject = json.getAsJsonObject();
            JsonElement namespace = jsonObject.get("namespace");
            JsonElement key = jsonObject.get("key");
            if (namespace == null) {
                return null;
            }
            if (key == null) {
                return null;
            }
            return new NamespacedKeyDTO(namespace.getAsString(), key.getAsString());
        }
        return null;
    }
}
