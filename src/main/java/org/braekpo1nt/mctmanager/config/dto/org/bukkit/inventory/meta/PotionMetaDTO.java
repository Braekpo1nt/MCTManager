package org.braekpo1nt.mctmanager.config.dto.org.bukkit.inventory.meta;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.braekpo1nt.mctmanager.config.validation.Validator;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class PotionMetaDTO extends ItemMetaDTOImpl {
    
    private @Nullable PotionType basePotionType;
    private @Nullable List<@Nullable PotionEffect> customEffects;
    private @Nullable List<@Nullable Boolean> customEffectsOverwrite;
    private @Nullable Color color;
    
    @SuppressWarnings("ConstantConditions")
    @Override
    public void validate(@NotNull Validator validator) {
        super.validate(validator);
        validator.notNull(basePotionType, "basePotionType");
        if (customEffects != null) {
            validator.validate(customEffectsOverwrite != null, "customEffectsOverwrite can't be null if customEffects is defined");
            validator.validate(customEffects.size() == customEffectsOverwrite.size(), "customEffects must be the same size as .customEffectsOverwrite");
            for (int i = 0; i < customEffectsOverwrite.size(); i++) {
                Boolean overwrite = customEffectsOverwrite.get(i);
                validator.notNull(overwrite, "customEffectsOverwrite[%s]", i);
            }
        }
    }
    
    @Override
    public ItemMeta toItemMeta(ItemMeta meta, Material type) {
        super.toItemMeta(meta, type);
        PotionMeta potionMeta = (PotionMeta) meta;
        if (basePotionType != null) {
            potionMeta.setBasePotionType(basePotionType);
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
