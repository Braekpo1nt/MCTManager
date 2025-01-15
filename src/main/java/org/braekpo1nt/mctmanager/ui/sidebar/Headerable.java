package org.braekpo1nt.mctmanager.ui.sidebar;

import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.participant.Participant;

public interface Headerable {
    void updatePersonalScore(Participant participant, Component contents);
    void updateTeamScore(Participant participant, Component contents);
}
