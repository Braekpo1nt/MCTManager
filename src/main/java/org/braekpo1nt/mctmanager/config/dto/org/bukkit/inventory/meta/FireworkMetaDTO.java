package org.braekpo1nt.mctmanager.config.dto.org.bukkit.inventory.meta;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.braekpo1nt.mctmanager.config.validation.Validator;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.FireworkEffect;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class FireworkMetaDTO extends ItemMetaDTOImpl{
    private int power;
    private @Nullable List<@Nullable FireworkEffect> fireworkEffects;
    private @Nullable FireworkEffect.Type fireworkType;
    private boolean trail;
    private boolean flicker;
    private @Nullable Color color;
    private FireworkEffect.Builder builder;
    
    @SuppressWarnings("ConstantConditions")
    @Override
    public void validate(@NotNull Validator validator) {
        super.validate(validator);
        validator.notNull(power, "power");
        if(fireworkEffects != null) {
            validator.validate(fireworkEffects.isEmpty(), "fireworkEffects must be defined");
        }
    }
    @Override
    public ItemMeta toItemMeta(ItemMeta meta, Material type) {
        super.toItemMeta(meta, type);
        FireworkMeta fireworkMeta = (FireworkMeta) meta;
        if(power > 0) {
            fireworkMeta.setPower(power);
        }
        if(fireworkEffects != null) {
            for(int i = 0; i < fireworkEffects.size(); i++) {
                FireworkEffect fireworkEffect = 
                        builder.flicker(flicker)
                                .withColor(color)
                                .trail(trail)
                                .with(fireworkType)
                                .build();
                        ;
                fireworkMeta.addEffect(fireworkEffect);
            }
        }
        return meta;
    }
}
