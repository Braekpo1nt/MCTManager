package org.braekpo1nt.mctmanager.commands.utils;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

class DoubleSerializer implements JsonSerializer<Double> {
    @Override
    public JsonElement serialize(Double src, java.lang.reflect.Type typeOfSrc, JsonSerializationContext context) {
        if (src == Math.floor(src)) {
            // If the double value is equivalent to an integer, convert it to an integer
            return new JsonPrimitive((int) src.doubleValue());
        } else {
            return new JsonPrimitive(src);
        }
    }
}
