package org.braekpo1nt.mctmanager.display.delegates;

import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.display.Renderer;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

/**
 * A convenience interface for implementing {@link HasText} on a {@link Renderer} which
 * is composed of multiple {@link HasText} renderers
 */
public interface HasTextComposite extends HasTextSingleton {
    /**
     * @return All the {@link HasText} implementations which make up this composite
     */
    @NotNull Collection<? extends HasText> getHasTexts();
    
    /**
     * Sets the text of all {@link #getHasTexts()} elements
     * @param text the text to set to
     */
    @Override
    default void setText(@NotNull Component text) {
        getHasTexts().forEach(r -> r.setText(text));
    }
}
