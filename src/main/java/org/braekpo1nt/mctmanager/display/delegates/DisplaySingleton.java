package org.braekpo1nt.mctmanager.display.delegates;

import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.display.Renderer;
import org.bukkit.Color;
import org.bukkit.entity.Display;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A convenience interface for implementing {@link DisplayDelegate} on a {@link Renderer} which
 * is composed of a single {@link DisplayDelegate} renderer
 */
public interface DisplaySingleton extends DisplayDelegate {
    @NotNull DisplayDelegate getDisplay();
    
    // DisplayEntityRenderer
    @Override
    default void setGlowing(boolean glowing) {
        getDisplay().setGlowing(glowing);
    }
    
    @Override
    default void setGlowColor(@NotNull Color glowColor) {
        getDisplay().setGlowColor(glowColor);
    }
    
    @Override
    default void setBrightness(@Nullable Display.Brightness brightness) {
        getDisplay().setBrightness(brightness);
    }
    
    @Override
    default void setInterpolationDuration(int interpolationDuration) {
        getDisplay().setInterpolationDuration(interpolationDuration);
    }
    
    @Override
    default void setTeleportDuration(int teleportDuration) {
        getDisplay().setTeleportDuration(teleportDuration);
    }
    
    @Override
    default @NotNull Color getGlowColor() {
        return getDisplay().getGlowColor();
    }
    
    @Override
    default @Nullable Display.Brightness getBrightness() {
        return getDisplay().getBrightness();
    }
    
    @Override
    default int getInterpolationDuration() {
        return getDisplay().getInterpolationDuration();
    }
    
    @Override
    default int getTeleportDuration() {
        return getDisplay().getTeleportDuration();
    }
    
    @Override
    default void setBillboard(Display.@NotNull Billboard billboard) {
        getDisplay().setBillboard(billboard);
    }
    
    @Override
    default void customName(@Nullable Component customName) {
        getDisplay().customName(customName);
    }
    
    @Override
    default void setCustomNameVisible(boolean customNameVisible) {
        getDisplay().setCustomNameVisible(customNameVisible);
    }
    
    @Override
    default boolean isGlowing() {
        return getDisplay().isGlowing();
    }
    // Renderer
    
    @Override
    default void show() {
        getDisplay().show();
    }
    
    @Override
    default boolean showing() {
        return getDisplay().showing();
    }
    
    @Override
    default void hide() {
        getDisplay().hide();
    }
    
    @Override
    default @NotNull Display.Billboard getBillboard() {
        return getDisplay().getBillboard();
    }
    
    @Override
    default @Nullable Component customName() {
        return getDisplay().customName();
    }
    
    @Override
    default boolean isCustomNameVisible() {
        return getDisplay().isCustomNameVisible();
    }
}
