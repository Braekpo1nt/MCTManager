package org.braekpo1nt.mctmanager.games.game.config.inventory.meta;

import com.destroystokyo.paper.Namespaced;
import com.google.gson.JsonElement;
import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.games.game.config.ConfigUtil;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionEffect;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class ItemMetaDTO {
    /**
     * A JsonElement to be converted to a Component
     */
    protected @Nullable JsonElement displayName;
    /**
     * A list of JsonElements to be converted to a list of Components
     */
    protected @Nullable List<JsonElement> lore;
    protected @Nullable Set<ItemFlag> itemFlags;
    protected boolean unbreakable;
    protected @Nullable Map<Attribute, List<AttributeModifier>> attributeModifiers;
    protected @Nullable Set<Namespaced> destroyableKeys;
    protected @Nullable Set<Namespaced> placeableKeys;
    
    // PotionMeta
    private PotionData basePotionData;
    private List<PotionEffect> customEffects;
    private List<Boolean> customEffectsOverwrite;
    private Color color;
    
    /**
     * 
     * @param loreDTO the list of {@link JsonElement}s that represent the 
     *                list of {@link Component}s for an {@link ItemMeta}'s lore
     * @return the loreDTO as a list of {@link Component}s for use as an {@link ItemMeta}'s lore
     * @throws IllegalArgumentException if any of the given loreDTO elements can't be parsed as a {@link Component}
     */
    public static @NotNull List<Component> toLore(@NotNull List<@NotNull JsonElement> loreDTO) {
        try {
            return ConfigUtil.toComponents(loreDTO);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("lore is invalid", e);
        }
    }
    
    /**
     * Imbues the provided ItemMeta with the attributes of this ItemMetaDTO
     * @param meta the ItemMeta to be modified
     * @return the provided ItemMeta, after all the attributes have been set 
     * to this ItemMetaDTO's attributes
     */
    public ItemMeta toItemMeta(ItemMeta meta, Material type) {
        if (displayName != null) {
            Component newDisplayName = ConfigUtil.toComponent(displayName);
            meta.displayName(newDisplayName);
        }
        if (lore != null) {
            meta.lore(ItemMetaDTO.toLore(lore));
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
        switch (type) {
            case POTION, SPLASH_POTION, LINGERING_POTION -> {
                PotionMeta potionMeta = (PotionMeta) meta;
                if (basePotionData != null) {
                    potionMeta.setBasePotionData(basePotionData);
                }
                if (customEffects != null && customEffectsOverwrite != null) {
                    for (int i = 0; i < customEffects.size(); i++) {
                        PotionEffect customEffect = customEffects.get(i);
                        Boolean overwrite = customEffectsOverwrite.get(i);
                        if (customEffect != null && overwrite != null) {
                            potionMeta.addCustomEffect(customEffect, overwrite);
                        }
                    }
                }
                potionMeta.setColor(color);
                Bukkit.getLogger().info("PotionMeta called");
            }
        }
        Bukkit.getLogger().info("toItemStack called");
        return meta;
    }
    
}
