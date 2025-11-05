package org.braekpo1nt.mctmanager.config.dto.org.bukkit.inventory.meta;

import lombok.Data;
import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.config.dto.org.bukkit.inventory.meta.components.CustomModelDataComponentDTO;
import org.braekpo1nt.mctmanager.config.dto.org.bukkit.persistence.PersistentDataContainerDTO;
import org.braekpo1nt.mctmanager.config.validation.Validatable;
import org.braekpo1nt.mctmanager.config.validation.Validator;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.components.CustomModelDataComponent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Data
public class ItemMetaDTOImpl implements ItemMetaDTO, Validatable {
    /**
     * A JsonElement to be converted to a Component
     */
    protected @Nullable Component displayName;
    /**
     * A list of JsonElements to be converted to a list of Components
     */
    protected @Nullable List<@Nullable Component> lore;
    protected @Nullable Set<ItemFlag> itemFlags;
    protected boolean unbreakable;
    protected @Nullable Map<Attribute, List<AttributeModifier>> attributeModifiers;
    /**
     * @deprecated because paper is deprecating {@link ItemMeta#setCustomModelData(Integer)}
     */
    @Deprecated
    protected @Nullable Integer customModelData;
    protected @Nullable CustomModelDataComponentDTO customModelDataComponent;
    protected @Nullable PersistentDataContainerDTO persistentDataContainer;
    
    @Override
    public Component displayName() {
        return displayName;
    }
    
    @Override
    public void displayName(final @Nullable Component displayName) {
        this.displayName = displayName;
    }
    
    @Override
    public boolean hasDisplayName() {
        return this.displayName != null;
    }
    
    @Override
    public List<Component> lore() {
        return lore;
    }
    
    @Override
    public void lore(@Nullable List<Component> lore) {
        this.lore = lore;
    }
    
    @Override
    public boolean hasLore() {
        return this.lore != null;
    }
    
    /**
     * @deprecated because paper is deprecating {@link ItemMeta#hasCustomModelData()}
     */
    @Deprecated
    @Override
    public boolean hasCustomModelData() {
        return this.customModelData != null;
    }
    
    /**
     * Check if this ItemMetaDTOImpl has customModelData first with the {@link ItemMetaDTOImpl#hasCustomModelData()}
     * @return the customModelData, if this ItemMetaDTOImpl has one
     * @throws IllegalStateException if {@link ItemMetaDTOImpl#customModelData} is null
     * @deprecated because paper is deprecating {@link ItemMeta#getCustomModelData()}
     */
    @Deprecated
    @Override
    public int getCustomModelData() {
        if (this.customModelData == null) {
            throw new IllegalStateException("customModelData is null (this ItemMetaDTO does not have customModelData)");
        }
        return this.customModelData;
    }
    
    @Override
    public @Nullable PersistentDataContainerDTO getPersistentDataContainerDTO() {
        return persistentDataContainer;
    }
    
    @Override
    public void validate(@NotNull Validator validator) {
        if (customModelDataComponent != null) {
            customModelDataComponent.validate(validator.path("customModelDataComponent"));
        }
    }
    
    /**
     * Imbues the provided ItemMeta with the attributes of this ItemMetaDTO
     * @param meta the ItemMeta to be modified
     * @return the provided ItemMeta, after all the attributes have been set
     * to this ItemMetaDTO's attributes
     */
    @Override
    public ItemMeta toItemMeta(ItemMeta meta, Material type) {
        // TODO: Implement "components" category from Recipe json.misode.github.io/recipe/
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
        if (persistentDataContainer != null) {
            persistentDataContainer.toPersistentDataContainer(meta.getPersistentDataContainer());
        }
        if (customModelDataComponent != null) {
            meta.setCustomModelDataComponent(
                    customModelDataComponent.toCustomModelDataComponent(
                            meta.getCustomModelDataComponent()
                    )
            );
        }
        // TODO: remove this deprecated usage of customModelData
        if (customModelData != null) {
            CustomModelDataComponent component = meta.getCustomModelDataComponent();
            List<Float> newFloats = new ArrayList<>(component.getFloats());
            newFloats.add((float) customModelData);
            meta.setCustomModelDataComponent(component);
        }
        return meta;
    }
    
}
