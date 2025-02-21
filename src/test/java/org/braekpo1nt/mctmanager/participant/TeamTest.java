package org.braekpo1nt.mctmanager.participant;

import net.kyori.adventure.text.format.NamedTextColor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Collections;

class TeamTest {
    
    @Test
    void equals() {
        Team team = new MCTTeam("yellow", "Yellow", NamedTextColor.YELLOW, Collections.emptySet());
        TeamData<Participant> teamData = new TeamData<>(team);
        
        Assertions.assertEquals(team, teamData);
    }
    
}
