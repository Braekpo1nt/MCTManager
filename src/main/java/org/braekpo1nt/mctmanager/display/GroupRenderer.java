package org.braekpo1nt.mctmanager.display;

import lombok.Getter;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Used to hold other displays
 */
public class GroupRenderer implements Renderer {
    
    private final List<Renderer> children = new ArrayList<>();
    @Getter
    private final @NotNull Location location;
    
    public GroupRenderer(@NotNull Location location) {
        this.location = location;
    }
    
    public void addChild(@NotNull Renderer child) {
        children.add(child);
    }
    
    @Override
    public void show() {
        for (Renderer child : children) {
            child.show();
        }
    }
    
    @Override
    public void hide() {
        for (Renderer child : children) {
            child.hide();
        }
    }
}
