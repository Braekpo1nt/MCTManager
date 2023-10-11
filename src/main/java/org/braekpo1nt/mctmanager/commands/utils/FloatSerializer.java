package org.braekpo1nt.mctmanager.commands.utils;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

class FloatSerializer implements JsonSerializer<Float> {
    @Override
    public JsonElement serialize(Float src, java.lang.reflect.Type typeOfSrc, JsonSerializationContext context) {
        double value = src.doubleValue();
        if (value == Math.floor(value)) {
            // If the value is equivalent to an integer, convert it to an integer
            return new JsonPrimitive((int) value);
        } else {
            return new JsonPrimitive(value);
        }
    }
}
