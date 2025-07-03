package org.braekpo1nt.mctmanager.display.delegates;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

/**
 * The interface for any Renderer which allows you to set attributes specific to
 * {@link org.bukkit.entity.TextDisplay}, such as text or background color
 */
public interface TextDisplayDelegate extends DisplayDelegate {
    void setText(@NotNull Component text);
}
