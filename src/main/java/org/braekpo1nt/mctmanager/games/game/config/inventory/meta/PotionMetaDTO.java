package org.braekpo1nt.mctmanager.games.game.config.inventory.meta;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionEffect;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@Getter
@AllArgsConstructor
public class PotionMetaDTO extends ItemMetaDTO {
    
    private @Nullable PotionData basePotionData;
    private @Nullable List<@Nullable PotionEffect> customEffects;
    private @Nullable List<@Nullable Boolean> customEffectsOverwrite;
    private @Nullable Color color;
    
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
