package org.braekpo1nt.mctmanager.ui.topbar.components;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.jetbrains.annotations.NotNull;

/**
 * A component representing a single player's readyup status
 * Can be converted into a {@link Component} for display to a user
 */
public class PlayerReadyUpComponent {
    private static final @NotNull TextComponent READY = Component.empty()
            .append(Component.text("Ready"))
            .color(NamedTextColor.GREEN);
    
    private static final @NotNull TextComponent NOT_READY = Component.empty()
            .append(Component.text("Not Ready"))
            .color(NamedTextColor.RED);
    
    private Boolean ready;
    
    public void setReady(Boolean ready) {
        this.ready = ready;
    }
    
    public Component toComponent() {
        if (ready == null) {
            return Component.empty();
        }
        if (ready) {
            return READY;
        } else {
            return NOT_READY;
        }
    }
}
