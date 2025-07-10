package org.braekpo1nt.mctmanager.display.delegates;

import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.display.Renderer;
import org.bukkit.Color;
import org.bukkit.entity.Display;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * The interface for any Renderer which requires the ability set attributes specific to
 * {@link Display} entities, such as glow color and brightness
 */
public interface DisplayDelegate extends Renderer {
    void setGlowColor(@NotNull Color glowColor);
    void setBrightness(@Nullable Display.Brightness brightness);
    void setInterpolationDuration(int interpolationDuration);
    void setTeleportDuration(int teleportDuration);
    void setGlowing(boolean glowing);
    void setBillboard(@NotNull Display.Billboard billboard);
    void customName(@Nullable Component customName);
    void setCustomNameVisible(boolean customNameVisible);
    
    @NotNull Color getGlowColor();
    @Nullable Display.Brightness getBrightness();
    int getInterpolationDuration();
    int getTeleportDuration();
    boolean isGlowing();
    @NotNull Display.Billboard getBillboard();
    @Nullable Component customName();
    boolean isCustomNameVisible();
}
