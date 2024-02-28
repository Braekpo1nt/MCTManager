package org.braekpo1nt.mctmanager.games.game.config;

import com.destroystokyo.paper.Namespaced;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.enchantments.EnchantmentWrapper;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class ItemStackDTO {
    private Material type;
    private int amount;
    private Component displayName;
    private List<Component> lore;
    private Map<String, Integer> enchants;
    private Set<ItemFlag> itemFlags;
    private boolean unbreakable;
    private Map<Attribute, List<AttributeModifier>> attributeModifiers;
    private Set<Namespaced> destroyableKeys;
    private Set<Namespaced> placeableKeys;
    
    public ItemStack toItemStack() {
        ItemStack stack = new ItemStack(type, amount);
        ItemMeta meta = stack.getItemMeta();
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
        return stack;
    }
}
