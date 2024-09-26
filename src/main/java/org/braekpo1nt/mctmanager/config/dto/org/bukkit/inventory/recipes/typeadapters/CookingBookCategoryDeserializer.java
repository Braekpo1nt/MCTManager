package org.braekpo1nt.mctmanager.config.dto.org.bukkit.inventory.recipes.typeadapters;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import org.bukkit.inventory.recipe.CookingBookCategory;

import java.lang.reflect.Type;
import java.util.Locale;

public class CookingBookCategoryDeserializer implements JsonDeserializer<CookingBookCategory> {
    @Override
    public CookingBookCategory deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        String categoryStr = json.getAsString();
        if (categoryStr == null) {
            return null;
        }
        return CookingBookCategory.valueOf(categoryStr.toUpperCase(Locale.ROOT));
    }
}
