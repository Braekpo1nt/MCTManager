package org.braekpo1nt.mctmanager.display;

import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.entity.Display;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

public class TitledRenderer<R extends Renderer> implements Renderer {
    
    @Getter
    private final @NotNull R renderer;
    @Getter
    private final TransientTextDisplayRenderer titleRenderer;
    private final Function<R, @NotNull Location> locationUpdater;
    
    public TitledRenderer(@NotNull R renderer, @Nullable Component title, @NotNull Function<R, @NotNull Location> locationUpdater) {
        this.renderer = renderer;
        this.locationUpdater = locationUpdater;
        this.titleRenderer = TransientTextDisplayRenderer.builder()
                .text(title)
                .location(locationUpdater.apply(renderer))
                .billboard(Display.Billboard.CENTER)
                .build();
    }
    
    @Override
    public @NotNull Location getLocation() {
        return renderer.getLocation();
    }
    
    public void updateTitleLocation() {
        titleRenderer.setLocation(locationUpdater.apply(renderer));
    }
    
    public void updateTitle(@Nullable Component title) {
        this.titleRenderer.setText(title);
    }
    
    @Override
    public void show() {
        renderer.show();
        titleRenderer.show();
    }
    
    @Override
    public boolean showing() {
        return renderer.showing();
    }
    
    @Override
    public void hide() {
        renderer.hide();
        titleRenderer.hide();
    }
}
