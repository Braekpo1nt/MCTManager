package org.braekpo1nt.mctmanager.display.delegates;

import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.display.Renderer;
import org.jetbrains.annotations.NotNull;

/**
 * A convenience interface for implementing {@link TextDisplayDelegate} on a {@link Renderer} which
 * is composed of a single {@link TextDisplayDelegate} renderer
 */
public interface TextDisplaySingleton extends TextDisplayDelegate {
    
    @NotNull TextDisplayDelegate getTextDisplay();
    
    @Override
    default void setText(@NotNull Component text) {
        getTextDisplay().setText(text);
    }
}
