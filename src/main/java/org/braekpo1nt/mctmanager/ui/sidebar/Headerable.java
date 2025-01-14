package org.braekpo1nt.mctmanager.ui.sidebar;

import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.participant.Participant;
import org.bukkit.entity.Player;

public interface Headerable {
    // TODO: Participant replace both of these Player arguments with Participant arguments
    default void updatePersonalScore(Participant participant, Component contents) {
        updatePersonalScore(participant, contents);
    }
    
    /**
     * @deprecated in favor of {@link #updatePersonalScore(Participant, Component)}
     */
    @Deprecated
    void updatePersonalScore(Player participant, Component contents);
    void updateTeamScore(Player participant, Component contents);
}
