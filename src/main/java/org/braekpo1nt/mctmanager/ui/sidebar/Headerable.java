package org.braekpo1nt.mctmanager.ui.sidebar;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

public interface Headerable {
    void updatePersonalScore(Player participant, Component contents);
    void updateTeamScore(Player participant, Component contents);
}
