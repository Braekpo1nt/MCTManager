package org.braekpo1nt.mctmanager.participant;

import net.kyori.adventure.text.format.NamedTextColor;
import org.braekpo1nt.mctmanager.games.game.clockwork.ClockworkTeam;
import org.braekpo1nt.mctmanager.games.game.farmrush.Arena;
import org.braekpo1nt.mctmanager.games.game.farmrush.FarmRushTeam;
import org.braekpo1nt.mctmanager.games.game.survivalgames.SurvivalGamesTeam;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.mockito.Mockito.mock;

class TeamTest {
    
    static Team team;
    
    @BeforeAll
    static void setup() {
        team = new MCTTeam("yellow", "Yellow", NamedTextColor.YELLOW, Collections.emptySet(), 0);
    }
    
    @Test
    void teamDataEquals() {
        TeamData<Participant> teamData = new TeamData<>(team);
        
        Assertions.assertEquals(team, teamData);
    }
    
    @Test
    void clockworkEquals() {
        ClockworkTeam teamData = new ClockworkTeam(team, 0);
        
        Assertions.assertEquals(team, teamData);
    }
    
    @Test
    void farmRushEquals() {
        FarmRushTeam teamData = new FarmRushTeam(team, mock(Arena.class), 0);
        
        Assertions.assertEquals(team, teamData);
    }
    
    @Test
    void survivalGamesEquals() {
        SurvivalGamesTeam teamData = new SurvivalGamesTeam(team, 0);
        
        Assertions.assertEquals(team, teamData);
    }
    
}
