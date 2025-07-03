package org.braekpo1nt.mctmanager.display.delegates;

import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.display.Renderer;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

/**
 * A convenience interface for implementing {@link TextDisplayDelegate} on a {@link Renderer} which
 * is composed of multiple {@link TextDisplayDelegate} renderers
 */
public interface TextDisplayComposite extends DisplayComposite, TextDisplayDelegate {
    @Override
    @NotNull Collection<? extends TextDisplayDelegate> getRenderers();
    
    @Override
    default void setText(@NotNull Component text) {
        getRenderers().forEach(r -> r.setText(text));
    }
}
