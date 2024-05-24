package org.braekpo1nt.mctmanager.config.dto.org.bukkit.inventory.meta;

import com.google.common.base.Preconditions;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.braekpo1nt.mctmanager.config.validation.Validator;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionEffect;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class PotionMetaDTO extends ItemMetaDTOImpl {
    
    private @Nullable PotionData basePotionData;
    private @Nullable List<@Nullable PotionEffect> customEffects;
    private @Nullable List<@Nullable Boolean> customEffectsOverwrite;
    private @Nullable Color color;
    
    @SuppressWarnings("ConstantConditions")
    @Override
    public void validate(Validator validator) {
        super.validate(validator);
        validator.notNull(basePotionData, "basePotionData");
        validator.validate(basePotionData.getType() != null, "basePotionData.type can't be null");
        if (customEffects != null) {
            validator.validate(customEffectsOverwrite != null, "customEffectsOverwrite can't be null if customEffects is defined");
            validator.validate(customEffects.size() == customEffectsOverwrite.size(), "customEffects must be the same size as .customEffectsOverwrite");
            for (int i = 0; i < customEffectsOverwrite.size(); i++) {
                Boolean overwrite = customEffectsOverwrite.get(i);
                validator.notNull(overwrite, "customEffectsOverwrite[%s]", i);
            }
        }
    }
    
    /**
     * @deprecated in favor of {@link org.braekpo1nt.mctmanager.config.validation.Validatable}
     */
    @Deprecated
    @SuppressWarnings("ConstantConditions")
    @Override
    public void isValid() {
        super.isValid();
        Preconditions.checkArgument(basePotionData != null, "basePotionData can't be null");
        Preconditions.checkArgument(basePotionData.getType() != null, "basePotionData.type can't be null");
        if (customEffects != null) {
            Preconditions.checkArgument(customEffectsOverwrite != null, "customEffectsOverwrite can't be null if customEffects is defined");
            Preconditions.checkArgument(customEffects.size() == customEffectsOverwrite.size());
            for (int i = 0; i < customEffectsOverwrite.size(); i++) {
                Boolean overwrite = customEffectsOverwrite.get(i);
                Preconditions.checkArgument(overwrite != null, "customEffectsOverwrite[%s] can't be null", i);
            }
        }
    }
    
    @Override
    public ItemMeta toItemMeta(ItemMeta meta, Material type) {
        super.toItemMeta(meta, type);
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
        return meta;
    }
}
