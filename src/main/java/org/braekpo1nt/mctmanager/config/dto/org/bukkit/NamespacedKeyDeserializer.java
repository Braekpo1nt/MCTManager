package org.braekpo1nt.mctmanager.config.dto.org.bukkit;

import com.google.gson.*;
import org.braekpo1nt.mctmanager.config.ConfigUtils;
import org.bukkit.NamespacedKey;

import java.lang.reflect.Type;

public class NamespacedKeyDeserializer implements JsonDeserializer<NamespacedKey> {
    @Override
    public NamespacedKey deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        if (json.isJsonPrimitive()) {
            String namespacedKeyStr = json.getAsString();
            if (namespacedKeyStr == null) {
                return null;
            }
            int indexOfColon = namespacedKeyStr.indexOf(':');
            String namespace;
            String key;
            if (indexOfColon < 0) {
                namespace = NamespacedKey.MINECRAFT_NAMESPACE;
                key = namespacedKeyStr;
            } else {
                namespace = namespacedKeyStr.substring(0, indexOfColon);
                key = namespacedKeyStr.substring(indexOfColon+1);
                if (!ConfigUtils.isValidNamespace(namespace)) {
                    throw new JsonParseException(String.format("namespace must be [a-z0-9._-]: %s", namespace));
                }
            }
            if (!ConfigUtils.isValidKey(key)) {
                throw new JsonParseException(String.format("key must be [a-z0-9/._-]: %s", key));
            }
            return new NamespacedKey(namespace, key);
        } else if (json.isJsonObject()) {
            JsonObject jsonObject = json.getAsJsonObject();
            JsonElement namespaceElement = jsonObject.get("namespace");
            JsonElement keyElement = jsonObject.get("key");
            String namespace;
            if (namespaceElement == null) {
                namespace = "minecraft";
            } else {
                namespace = namespaceElement.getAsString();
                if (!ConfigUtils.isValidNamespace(namespace)) {
                    throw new JsonParseException(String.format("namespace must be [a-z0-9._-]: %s", namespace));
                }
            }
            if (keyElement == null) {
                return null;
            }
            String key = keyElement.getAsString();
            if (!ConfigUtils.isValidKey(key)) {
                throw new JsonParseException(String.format("key must be [a-z0-9/._-]: %s", key));
            }
            return new NamespacedKey(namespace, key);
        }
        return null;
    }
}
