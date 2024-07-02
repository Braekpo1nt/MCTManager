package org.braekpo1nt.mctmanager.ui.sidebar;

import lombok.Data;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;


@Data
public class KeyLine {
    private final @NotNull String key;
    private final @NotNull Component contents;
    
    public KeyLine(@NotNull String key, @NotNull String contents) {
        this.key = key;
        this.contents = Component.text(contents);
    }
    
}
