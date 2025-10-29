package org.braekpo1nt.mctmanager.games.game.spleef;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.jetbrains.annotations.NotNull;

/**
 * Allows {@link DecayManager} to communicate with the context
 */
public interface SpleefInterface {
    void messageAllParticipants(@NotNull Component message);
    
    void titleAllParticipants(@NotNull Title title);
    
    void setShouldGivePowerups(boolean shouldGivePowerups);
}
