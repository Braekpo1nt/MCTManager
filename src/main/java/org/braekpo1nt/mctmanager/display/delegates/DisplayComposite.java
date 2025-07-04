package org.braekpo1nt.mctmanager.display.delegates;

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
public interface DisplayComposite<T extends DisplayDelegate> extends DisplayDelegate {
    @NotNull Collection<? extends DisplayDelegate> getRenderers();
    
    /**
     * @return a single, primary renderer. Used to get attributes
     * about this {@link DisplayDelegate}
     */
    @NotNull T getPrimaryRenderer();
    
    // DisplayEntityRenderer
    @Override
    default void setGlowing(boolean glowing) {
        getRenderers().forEach(r -> r.setGlowing(glowing));
    }
    
    @Override
    default void setGlowColor(@NotNull Color glowColor) {
        getRenderers().forEach(r -> r.setGlowColor(glowColor));
    }
    
    @Override
    default void setBrightness(@Nullable Display.Brightness brightness) {
        getRenderers().forEach(r -> r.setBrightness(brightness));
    }
    
    @Override
    default void setInterpolationDuration(int interpolationDuration) {
        getRenderers().forEach(r -> r.setInterpolationDuration(interpolationDuration));
    }
    
    @Override
    default void setTeleportDuration(int teleportDuration) {
        getRenderers().forEach(r -> r.setTeleportDuration(teleportDuration));
    }
    
    // Renderer
    @Override
    default void show() {
        getRenderers().forEach(Renderer::show);
    }
    
    @Override
    default void hide() {
        getRenderers().forEach(Renderer::hide);
    }
    
    @Override
    default @NotNull Color getGlowColor() {
        return getPrimaryRenderer().getGlowColor();
    }
    
    @Override
    default @Nullable Display.Brightness getBrightness() {
        return getPrimaryRenderer().getBrightness();
    }
    
    @Override
    default int getInterpolationDuration() {
        return getPrimaryRenderer().getInterpolationDuration();
    }
    
    @Override
    default int getTeleportDuration() {
        return getPrimaryRenderer().getTeleportDuration();
    }
    
    @Override
    default boolean isGlowing() {
        return getPrimaryRenderer().isGlowing();
    }
}
