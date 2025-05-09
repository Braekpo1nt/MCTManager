package org.braekpo1nt.mctmanager.participant;

import lombok.Data;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;

@Data
public class ColorAttributes {
    private final @NotNull Material powder;
    private final @NotNull Material concrete;
    private final @NotNull Material stainedGlass;
    private final @NotNull Material stainedGlassPane;
    private final @NotNull Material banner;
    private final @NotNull Material wool;
}
