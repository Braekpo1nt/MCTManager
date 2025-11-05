package org.braekpo1nt.mctmanager.ui;

import org.braekpo1nt.mctmanager.participant.Participant;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public interface UIManager {
    
    default void showPlayer(@NotNull Participant participant) {
        showPlayer(participant.getPlayer());
    }
    
    void showPlayer(@NotNull Player player);
    
    default void hidePlayer(@NotNull Participant participant) {
        hidePlayer(participant.getPlayer());
    }
    
    void hidePlayer(@NotNull Player player);
    
    /**
     * <p>This method should reset this {@link UIManager} such that it will not
     * be used again.</p>
     * <p>Remove all viewers/players/admins, clear all contents,
     * remove all listeners, stop all tasks, etc.</p>
     */
    void cleanup();
    
}
