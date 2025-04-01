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
    
    void clear();
    
}
