package org.braekpo1nt.mctmanager.ui.sidebar;

import org.bukkit.entity.Player;

public interface Headerable {
    void updatePersonalScore(Player participant, String contents);
    void updateTeamScore(Player participant, String contents);
}
