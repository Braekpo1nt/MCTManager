package org.braekpo1nt.mctmanager.display.delegates;

import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.display.Renderer;
import org.bukkit.Color;
import org.bukkit.entity.Display;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

/**
 * A convenience interface for implementing {@link DisplayDelegate} on a {@link Renderer} which
 * is composed of multiple {@link DisplayDelegate} renderers
 * @param <T> The type of the primary renderer, must extend {@link DisplayDelegate}
 *           so that it can delegate its attributes.
 */
public interface DisplayComposite extends DisplaySingleton {
    @NotNull Collection<? extends DisplayDelegate> getDisplays();
    
    // DisplayEntityRenderer
    @Override
    default void customName(@Nullable Component customName) {
        getDisplay().customName(customName);
    }
    
    @Override
    default void setGlowing(boolean glowing) {
        getDisplays().forEach(r -> r.setGlowing(glowing));
    }
    
    @Override
    default void setGlowColor(@NotNull Color glowColor) {
        getDisplays().forEach(r -> r.setGlowColor(glowColor));
    }
    
    @Override
    default void setBrightness(@Nullable Display.Brightness brightness) {
        getDisplays().forEach(r -> r.setBrightness(brightness));
    }
    
    @Override
    default void setInterpolationDuration(int interpolationDuration) {
        getDisplays().forEach(r -> r.setInterpolationDuration(interpolationDuration));
    }
    
    @Override
    default void setTeleportDuration(int teleportDuration) {
        getDisplays().forEach(r -> r.setTeleportDuration(teleportDuration));
    }
    
    @Override
    default void setBillboard(Display.@NotNull Billboard billboard) {
        getDisplays().forEach(r -> r.setBillboard(billboard));
    }
    
    // Renderer
    @Override
    default void show() {
        getDisplays().forEach(Renderer::show);
    }
    
    @Override
    default void hide() {
        getDisplays().forEach(Renderer::hide);
    }
}
