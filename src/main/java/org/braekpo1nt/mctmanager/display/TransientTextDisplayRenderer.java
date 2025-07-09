package org.braekpo1nt.mctmanager.display;

import lombok.Builder;
import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.display.delegates.DisplayDelegate;
import org.braekpo1nt.mctmanager.display.delegates.DisplaySingleton;
import org.braekpo1nt.mctmanager.display.delegates.TextDisplayDelegate;
import org.braekpo1nt.mctmanager.display.delegates.TextDisplaySingleton;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.entity.Display;
import org.bukkit.util.Transformation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Designed to take up as few resources as possible by deleting the entity if
 * the text is set to null. 
 */
public class TransientTextDisplayRenderer implements DisplaySingleton, TextDisplaySingleton {
    
    private final TextDisplayEntityRenderer renderer;
    private @Nullable Component text;
    private boolean showing;
    
    @Builder
    public TransientTextDisplayRenderer(
            @NotNull Location location,
            boolean glowing,
            @Nullable Color glowColor,
            @Nullable Display.Brightness brightness,
            @Nullable Transformation transformation,
            int interpolationDuration,
            int teleportDuration,
            @Nullable Display.Billboard billboard,
            @Nullable Component text
    ) {
        this.showing = false;
        this.text = text;
        this.renderer = TextDisplayEntityRenderer.builder()
                .location(location)
                .text(text)
                .glowing(glowing)
                .glowColor(glowColor)
                .brightness(brightness)
                .transformation(transformation)
                .interpolationDuration(interpolationDuration)
                .teleportDuration(teleportDuration)
                .billboard(billboard)
                .build();
    }
    
    @Override
    public @NotNull DisplayDelegate getDisplay() {
        return renderer;
    }
    
    @Override
    public @NotNull TextDisplayDelegate getTextDisplay() {
        return renderer;
    }
    
    @Override
    public @NotNull Location getLocation() {
        return renderer.getLocation();
    }
    
    public void setLocation(@NotNull Location location) {
        renderer.setLocation(location);
    }
    
    @Override
    public void setText(@Nullable Component text) {
        renderer.setText((text != null) ? text : Component.empty());
        boolean shouldShow = text != null;
        boolean shouldNotShow = !shouldShow;
        boolean wasNotShowing = this.text == null;
        boolean wasShowing = !wasNotShowing;
        if (showing) {
            if (wasNotShowing && shouldShow) {
                renderer.show();
            } else if (wasShowing && shouldNotShow) {
                renderer.hide();
            }
        }
        this.text = text;
    }
    
    @Override
    public void show() {
        if (showing) {
            return;
        }
        showing = true;
        if (text == null) {
            return;
        }
        renderer.show();
    }
    
    @Override
    public boolean showing() {
        return showing;
    }
    
    @Override
    public void hide() {
        if (!showing) {
            return;
        }
        showing = false;
        if (text == null) {
            return;
        }
        renderer.hide();
    }
}
