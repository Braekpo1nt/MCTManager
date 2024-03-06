package org.braekpo1nt.mctmanager.games.game.config.inventory.meta;

import com.destroystokyo.paper.Namespaced;
import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.enchantments.EnchantmentWrapper;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ItemMetaDTO {
    protected JsonElement displayName;
    protected List<JsonElement> lore;
    protected Map<String, Integer> enchants;
    protected Set<ItemFlag> itemFlags;
    protected boolean unbreakable;
    protected Map<Attribute, List<AttributeModifier>> attributeModifiers;
    protected Set<Namespaced> destroyableKeys;
    protected Set<Namespaced> placeableKeys;
    
    /**
     * Imbues the provided ItemMeta with the attributes of this ItemMetaDTO
     * @param meta the ItemMeta to be modified
     * @return the provided ItemMeta, after all the attributes have been set 
     * to this ItemMetaDTO's attributes
     */
    public ItemMeta toItemMeta(ItemMeta meta) {
        Component newDisplayName = GsonComponentSerializer.gson().deserializeFromTree(displayName);
        meta.displayName(newDisplayName);
        List<Component> newLore = lore.stream().map(line -> GsonComponentSerializer.gson().deserializeFromTree(line)).toList();
        meta.lore(newLore);
        for (Map.Entry<String, Integer> enchant : enchants.entrySet()) {
            meta.addEnchant(new EnchantmentWrapper(enchant.getKey()), enchant.getValue(), true);
        }
        meta.addItemFlags(itemFlags.toArray(new ItemFlag[0]));
        meta.setUnbreakable(unbreakable);
        for (Map.Entry<Attribute, List<AttributeModifier>> entry : attributeModifiers.entrySet()) {
            for (AttributeModifier modifier : entry.getValue()) {
                meta.addAttributeModifier(entry.getKey(), modifier);
            }
        }
        meta.setDestroyableKeys(destroyableKeys);
        meta.setPlaceableKeys(placeableKeys);
        return meta;
    }
    
}
