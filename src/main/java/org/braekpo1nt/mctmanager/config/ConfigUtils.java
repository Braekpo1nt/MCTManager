package org.braekpo1nt.mctmanager.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.config.dto.net.kyori.adventure.text.ComponentAdapter;
import org.braekpo1nt.mctmanager.config.dto.org.braekpo1nt.mctmanager.geometry.GeometryDeserializer;
import org.braekpo1nt.mctmanager.config.dto.org.bukkit.MaterialDeserializer;
import org.braekpo1nt.mctmanager.config.dto.org.bukkit.NamespacedKeyDTO;
import org.braekpo1nt.mctmanager.config.dto.org.bukkit.NamespacedKeyDTODeserializer;
import org.braekpo1nt.mctmanager.config.dto.org.bukkit.NamespacedKeyDeserializer;
import org.braekpo1nt.mctmanager.config.dto.org.bukkit.inventory.meta.ItemMetaDTO;
import org.braekpo1nt.mctmanager.config.dto.org.bukkit.inventory.meta.ItemMetaDTODeserializer;
import org.braekpo1nt.mctmanager.config.dto.org.bukkit.inventory.recipes.*;
import org.braekpo1nt.mctmanager.config.dto.org.bukkit.inventory.recipes.typeadapters.CookingBookCategoryDeserializer;
import org.braekpo1nt.mctmanager.config.dto.org.bukkit.inventory.recipes.typeadapters.RecipeDTODeserializer;
import org.braekpo1nt.mctmanager.config.dto.org.bukkit.inventory.recipes.typeadapters.RecipeChoiceDTODeserializer;
import org.braekpo1nt.mctmanager.config.dto.org.bukkit.inventory.recipes.typeadapters.SmithingRecipeDTODeserializer;
import org.braekpo1nt.mctmanager.config.dto.org.bukkit.util.BoundingBoxDeserializer;
import org.braekpo1nt.mctmanager.geometry.Geometry;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Tag;
import org.bukkit.inventory.recipe.CookingBookCategory;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.Nullable;

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
            .registerTypeAdapter(RecipeChoiceDTO.class, new RecipeChoiceDTODeserializer())
            .registerTypeAdapter(NamespacedKeyDTO.class, new NamespacedKeyDTODeserializer())
            .registerTypeAdapter(NamespacedKey.class, new NamespacedKeyDeserializer())
//            .registerTypeAdapter(MaterialTagDTO.class, new TagDeserializer())
            ;
    
    public static final Gson GSON = GSON_BUILDER
            .create();
    
    public static final Gson PRETTY_GSON = GSON_BUILDER
            .setPrettyPrinting()
            .create();
    
    private ConfigUtils() {
        // do not instantiate
    }
    
    public static @Nullable Tag<Material> toTag(NamespacedKey namespacedKey) {
        Tag<Material> blockTag = Bukkit.getTag(Tag.REGISTRY_BLOCKS, namespacedKey, Material.class);
        if (blockTag != null) {
            return blockTag;
        }
        return Bukkit.getTag(Tag.REGISTRY_ITEMS, namespacedKey, Material.class);
    }
    
    public static boolean isValidKey(String key) {
        int len = key.length();
        if (len == 0) {
            return false;
        }
        
        for (int i = 0; i < len; i++) {
            if (!isValidKeyChar(key.charAt(i))) {
                return false;
            }
        }
        
        return true;
    }
    
    private static boolean isValidKeyChar(char c) {
        return isValidNamespaceChar(c) || c == '/';
    }
    
    public static boolean isValidNamespace(String namespace) {
        int len = namespace.length();
        if (len == 0) {
            return false;
        }
        
        for (int i = 0; i < len; i++) {
            if (!isValidNamespaceChar(namespace.charAt(i))) {
                return false;
            }
        }
        
        return true;
    }
    
    private static boolean isValidNamespaceChar(char c) {
        return (c >= 'a' && c <= 'z') || (c >= '0' && c <= '9') || c == '.' || c == '_' || c == '-';
    }
}
