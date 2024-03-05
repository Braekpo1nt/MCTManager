package org.braekpo1nt.mctmanager.games.game.config.inventory.meta;

import com.destroystokyo.paper.Namespaced;
import net.kyori.adventure.text.Component;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.EnchantmentWrapper;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class ItemMetaDTO {
    protected Component displayName;
    protected List<Component> lore;
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
        meta.displayName(displayName);
        meta.lore(lore);
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
