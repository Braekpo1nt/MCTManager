package org.braekpo1nt.mctmanager.ui.sidebar;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

public interface Headerable {
    // TODO: Participant replace both of these Player arguments with Participant arguments
    void updatePersonalScore(Player participant, Component contents);
    void updateTeamScore(Player participant, Component contents);
}
