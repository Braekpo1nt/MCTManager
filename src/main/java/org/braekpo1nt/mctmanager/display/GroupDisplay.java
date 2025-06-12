package org.braekpo1nt.mctmanager.display;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Used to hold other displays
 */
public class GroupDisplay implements Display {
    
    private final List<Display> children = new ArrayList<>();
    
    public void addChild(@NotNull Display child) {
        children.add(child);
    }
    
    @Override
    public void show() {
        for (Display child : children) {
            child.show();
        }
    }
    
    @Override
    public void hide() {
        for (Display child : children) {
            child.hide();
        }
    }
}
