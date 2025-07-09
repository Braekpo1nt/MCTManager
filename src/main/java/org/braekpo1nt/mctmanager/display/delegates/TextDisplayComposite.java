package org.braekpo1nt.mctmanager.display.delegates;

import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.display.Renderer;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

/**
 * A convenience interface for implementing {@link TextDisplayDelegate} on a {@link Renderer} which
 * is composed of multiple {@link TextDisplayDelegate} renderers
 */
public interface TextDisplayComposite extends TextDisplaySingleton {
    @NotNull Collection<? extends TextDisplayDelegate> getTextDisplays();
    
    @Override
    default void setText(@NotNull Component text) {
        getTextDisplays().forEach(r -> r.setText(text));
    }
}
