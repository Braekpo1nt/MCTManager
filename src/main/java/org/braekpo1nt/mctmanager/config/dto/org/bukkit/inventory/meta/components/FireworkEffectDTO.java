package org.braekpo1nt.mctmanager.config.dto.org.bukkit.inventory.meta.components;

import lombok.Data;
import lombok.Getter;
import org.braekpo1nt.mctmanager.config.validation.Validatable;
import org.braekpo1nt.mctmanager.config.validation.Validator;
import org.braekpo1nt.mctmanager.utils.ColorMap;
import org.bukkit.FireworkEffect;
import org.jetbrains.annotations.NotNull;

@Data
public class FireworkEffectDTO implements Validatable {
    @Getter private FireworkEffect.Type fireworkType;
    @Getter private boolean trail;
    @Getter private boolean flicker;
    @Getter private String color;
    
    @Override
    public void validate(@NotNull Validator validator) {
        validator.notNull(fireworkType, "fireworkType");
        validator.notNull(trail, "trail");
        validator.notNull(flicker, "flicker");
        validator.notNull(color, "color");
        validator.validate(ColorMap.hasNamedTextColor(color), "color is not a recognized color. It should be one of %s", ColorMap.getNamedTextColors());
        
        validator.validate(color != null, "must be a valid color");
    }
}
