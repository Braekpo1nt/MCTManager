package org.braekpo1nt.mctmanager.display;

import lombok.Builder;
import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.display.delegates.HasText;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.entity.Display;
import org.bukkit.entity.TextDisplay;
import org.bukkit.util.Transformation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A Renderer representing a {@link TextDisplay} entity
 */
public class TextDisplayEntityRenderer extends DisplayEntityRenderer<TextDisplay> implements HasText {
    
    private @NotNull Component text;
    
    @Builder
    public TextDisplayEntityRenderer(
            @NotNull Location location,
            @Nullable Component customName,
            boolean customNameVisible,
            boolean glowing, 
            @Nullable Color glowColor,
            @Nullable Display.Brightness brightness,
            @Nullable Transformation transformation, 
            int interpolationDuration, 
            int teleportDuration, 
            @Nullable Display.Billboard billboard,
            @Nullable Component text) {
        super(
                location, 
                customName,
                customNameVisible,
                glowing, 
                glowColor, 
                brightness, 
                transformation, 
                interpolationDuration, 
                teleportDuration, 
                billboard);
        this.text = (text != null) ? text : Component.empty();
    }
    
    @Override
    public @NotNull Class<TextDisplay> getClazz() {
        return TextDisplay.class;
    }
    
    @Override
    protected void show(@NotNull TextDisplay entity) {
        super.show(entity);
        entity.text(text);
    }
    
    @Override
    public void setText(@NotNull Component text) {
        this.text = text;
        if (entity == null) {
            return;
        }
        entity.text(text);
    }
}
