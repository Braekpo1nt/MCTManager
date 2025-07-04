package org.braekpo1nt.mctmanager.display.delegates;

import org.braekpo1nt.mctmanager.display.Renderer;
import org.bukkit.Color;
import org.bukkit.entity.Display;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A convenience interface for implementing {@link DisplayDelegate} on a {@link Renderer} which
 * is composed of a single {@link DisplayDelegate} renderer
 */
public interface DisplaySingleton<T extends DisplayDelegate> extends DisplayDelegate {
    @NotNull T getRenderer();
    
    // DisplayEntityRenderer
    @Override
    default void setGlowing(boolean glowing) {
        getRenderer().setGlowing(glowing);
    }
    
    @Override
    default void setGlowColor(@NotNull Color glowColor) {
        getRenderer().setGlowColor(glowColor);
    }
    
    @Override
    default void setBrightness(@Nullable Display.Brightness brightness) {
        getRenderer().setBrightness(brightness);
    }
    
    @Override
    default void setInterpolationDuration(int interpolationDuration) {
        getRenderer().setInterpolationDuration(interpolationDuration);
    }
    
    @Override
    default void setTeleportDuration(int teleportDuration) {
        getRenderer().setTeleportDuration(teleportDuration);
    }
    
    @Override
    default @NotNull Color getGlowColor() {
        return getRenderer().getGlowColor();
    }
    
    @Override
    default @Nullable Display.Brightness getBrightness() {
        return getRenderer().getBrightness();
    }
    
    @Override
    default int getInterpolationDuration() {
        return getRenderer().getInterpolationDuration();
    }
    
    @Override
    default int getTeleportDuration() {
        return getRenderer().getTeleportDuration();
    }
    
    @Override
    default boolean isGlowing() {
        return getRenderer().isGlowing();
    }
    
    // Renderer
    @Override
    default void show() {
        getRenderer().show();
    }
    
    @Override
    default void hide() {
        getRenderer().hide();
    }
}
