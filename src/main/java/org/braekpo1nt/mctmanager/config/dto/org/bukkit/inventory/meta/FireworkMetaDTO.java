package org.braekpo1nt.mctmanager.config.dto.org.bukkit.inventory.meta;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.braekpo1nt.mctmanager.config.dto.org.bukkit.inventory.meta.components.FireworkEffectDTO;
import org.braekpo1nt.mctmanager.config.validation.Validator;
import org.braekpo1nt.mctmanager.utils.ColorMap;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Material;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class FireworkMetaDTO extends ItemMetaDTOImpl {
    private int fireworkPower;
    private List<FireworkEffectDTO> fireworkEffects;
    
    @SuppressWarnings("ConstantConditions")
    
    @Override
    public void validate(@NotNull Validator validator) {
        super.validate(validator);
        validator.notNull(fireworkPower, "fireworkPower");
        if (fireworkEffects != null) {
            validator.notNull(fireworkEffects, "fireworkEffects");
        }
    }
    
    @Override
    public ItemMeta toItemMeta(ItemMeta meta, Material type) {
        super.toItemMeta(meta, type);
        FireworkMeta fireworkMeta = (FireworkMeta) meta;
        if (fireworkPower > 0) {
            fireworkMeta.setPower(fireworkPower);
        }
        if (fireworkEffects != null) {
            for (FireworkEffectDTO fireworkEffectDTO : fireworkEffects) {
                Color color = ColorMap.getColor(fireworkEffectDTO.getColor());
                FireworkEffect fireworkEffect =
                        FireworkEffect.builder().flicker(fireworkEffectDTO.isFlicker())
                                .withColor(color)
                                .trail(fireworkEffectDTO.isTrail())
                                .with(fireworkEffectDTO.getType())
                                .build();
                fireworkMeta.addEffect(fireworkEffect);
            }
        }
        return meta;
    }
}
