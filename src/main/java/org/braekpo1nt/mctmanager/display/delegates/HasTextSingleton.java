package org.braekpo1nt.mctmanager.display.delegates;

import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.display.Renderer;
import org.jetbrains.annotations.NotNull;

/**
 * A convenience interface for implementing {@link HasText} on a {@link Renderer} which
 * is composed of a single {@link HasText} renderer
 */
public interface HasTextSingleton extends HasText {
    
    /**
     * @return The implementation of {@link HasText} that makes up this singleton
     */
    @NotNull
    HasText getHasText();
    
    /**
     * {@inheritDoc}
     */
    @Override
    default void setText(@NotNull Component text) {
        getHasText().setText(text);
    }
}
