package org.braekpo1nt.mctmanager.games.game.spleef;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.jetbrains.annotations.NotNull;

public interface SpleefInterfaceDeleteMe {
    void messageAllParticipants(@NotNull Component message);
    void titleAllParticipants(@NotNull Title title);
    void setShouldGivePowerups(boolean shouldGivePowerups);
}
