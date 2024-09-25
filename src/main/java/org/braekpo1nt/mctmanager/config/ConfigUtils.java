package org.braekpo1nt.mctmanager.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.config.dto.net.kyori.adventure.text.ComponentAdapter;
import org.braekpo1nt.mctmanager.config.dto.org.braekpo1nt.mctmanager.geometry.GeometryDeserializer;
import org.braekpo1nt.mctmanager.config.dto.org.bukkit.MaterialDeserializer;
import org.braekpo1nt.mctmanager.config.dto.org.bukkit.inventory.meta.ItemMetaDTO;
import org.braekpo1nt.mctmanager.config.dto.org.bukkit.inventory.meta.ItemMetaDTODeserializer;
import org.braekpo1nt.mctmanager.config.dto.org.bukkit.inventory.recipes.*;
import org.braekpo1nt.mctmanager.config.dto.org.bukkit.util.BoundingBoxDeserializer;
import org.braekpo1nt.mctmanager.geometry.Geometry;
import org.bukkit.Material;
import org.bukkit.inventory.recipe.CookingBookCategory;
import org.bukkit.util.BoundingBox;

public class ConfigUtils {
    
    private static final GsonBuilder GSON_BUILDER = new GsonBuilder()
            .registerTypeAdapter(ItemMetaDTO.class, new ItemMetaDTODeserializer())
            .registerTypeAdapter(Component.class, new ComponentAdapter())
            .registerTypeAdapter(Geometry.class, new GeometryDeserializer())
            .registerTypeAdapter(BoundingBox.class, new BoundingBoxDeserializer())
            .registerTypeAdapter(Material.class, new MaterialDeserializer())
            .registerTypeAdapter(CookingBookCategory.class, new CookingBookCategoryDeserializer())
            .registerTypeAdapter(RecipeDTO.class, new RecipeDTODeserializer())
            .registerTypeAdapter(SmithingRecipeDTO.class, new SmithingRecipeDTODeserializer())
            ;
    
    public static final Gson GSON = GSON_BUILDER
            .create();
    
    public static final Gson PRETTY_GSON = GSON_BUILDER
            .setPrettyPrinting()
            .create();
    
    private ConfigUtils() {
        // do not instantiate
    }
}
