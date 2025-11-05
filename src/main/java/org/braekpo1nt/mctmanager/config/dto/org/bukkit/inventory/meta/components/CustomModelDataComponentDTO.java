package org.braekpo1nt.mctmanager.config.dto.org.bukkit.inventory.meta.components;

import lombok.Data;
import org.braekpo1nt.mctmanager.config.validation.Validatable;
import org.braekpo1nt.mctmanager.config.validation.Validator;
import org.bukkit.Color;
import org.bukkit.inventory.meta.components.CustomModelDataComponent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@Data
public class CustomModelDataComponentDTO implements Validatable {
    private @Nullable List<Float> floats;
    private @Nullable List<Boolean> flags;
    private @Nullable List<String> strings;
    private @Nullable List<Color> colors;
    
    @Override
    public void validate(@NotNull Validator validator) {
        if (floats != null) {
            validator.validate(!floats.contains(null), "floats can't contain any null elements");
        }
        if (flags != null) {
            validator.validate(!flags.contains(null), "flags can't contain any null elements");
        }
        if (strings != null) {
            validator.validate(!strings.contains(null), "strings can't contain any null elements");
        }
        if (colors != null) {
            validator.validate(!colors.contains(null), "colors can't contain any null elements");
        }
    }
    
    public CustomModelDataComponent toCustomModelDataComponent(CustomModelDataComponent component) {
        if (floats != null) {
            component.setFloats(floats);
        }
        if (flags != null) {
            component.setFlags(flags);
        }
        if (strings != null) {
            component.setStrings(strings);
        }
        if (colors != null) {
            component.setColors(colors);
        }
        return component;
    }
}
