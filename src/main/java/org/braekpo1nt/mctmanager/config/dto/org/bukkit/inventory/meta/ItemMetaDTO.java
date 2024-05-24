package org.braekpo1nt.mctmanager.config.dto.org.bukkit.inventory.meta;

import com.destroystokyo.paper.Namespaced;
import com.google.gson.JsonElement;
import lombok.Data;
import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.config.ConfigUtils;
import org.braekpo1nt.mctmanager.config.validation.Validatable;
import org.braekpo1nt.mctmanager.config.validation.Validator;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Data
public class ItemMetaDTO implements Validatable {
    /**
     * A JsonElement to be converted to a Component
     */
    protected @Nullable Component displayName;
    /**
     * A list of JsonElements to be converted to a list of Components
     */
    protected @Nullable List<Component> lore;
    protected @Nullable Set<ItemFlag> itemFlags;
    protected boolean unbreakable;
    protected @Nullable Map<Attribute, List<AttributeModifier>> attributeModifiers;
    protected @Nullable Set<Namespaced> destroyableKeys;
    protected @Nullable Set<Namespaced> placeableKeys;
    
    @Override
    public void validate(Validator validator) {
        // nothing to validate
    }
    
    public void isValid() {
        // nothing to validate
    }
    
    /**
     * Imbues the provided ItemMeta with the attributes of this ItemMetaDTO
     * @param meta the ItemMeta to be modified
     * @return the provided ItemMeta, after all the attributes have been set 
     * to this ItemMetaDTO's attributes
     */
    public ItemMeta toItemMeta(ItemMeta meta, Material type) {
        if (displayName != null) {
            meta.displayName(displayName);
        }
        if (lore != null) {
            meta.lore(lore);
        }
        if (itemFlags != null) {
            meta.addItemFlags(itemFlags.toArray(new ItemFlag[0]));
        }
        meta.setUnbreakable(unbreakable);
        if (attributeModifiers != null) {
            for (Map.Entry<Attribute, List<AttributeModifier>> entry : attributeModifiers.entrySet()) {
                for (AttributeModifier modifier : entry.getValue()) {
                    meta.addAttributeModifier(entry.getKey(), modifier);
                }
            }
        }
        if (destroyableKeys != null) {
            meta.setDestroyableKeys(destroyableKeys);
        }
        if (placeableKeys != null) {
            meta.setPlaceableKeys(placeableKeys);
        }
        return meta;
    }
    
}
