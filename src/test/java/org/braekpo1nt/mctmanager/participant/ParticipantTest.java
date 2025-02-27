package org.braekpo1nt.mctmanager.participant;

import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.games.game.capturetheflag.CTFParticipant;
import org.braekpo1nt.mctmanager.games.game.capturetheflag.match.CTFMatchParticipant;
import org.braekpo1nt.mctmanager.games.game.capturetheflag.match.CaptureTheFlagMatch;
import org.braekpo1nt.mctmanager.games.game.footrace.FootRaceParticipant;
import org.braekpo1nt.mctmanager.games.game.parkourpathway.ParkourParticipant;
import org.braekpo1nt.mctmanager.games.game.spleef.SpleefParticipant;
import org.braekpo1nt.mctmanager.games.game.survivalgames.SurvivalGamesParticipant;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ParticipantTest {
    
    static Player mockPlayer;
    
    @BeforeAll
    static void setup() {
        mockPlayer = mock(Player.class);
        when(mockPlayer.getName()).thenReturn("rstln");
        when(mockPlayer.getUniqueId()).thenReturn(UUID.randomUUID());
    }
    
    @Test
    void participantShouldEqualOfflineParticipant() {
        OfflineParticipant offlineParticipant = new OfflineParticipant(
                mockPlayer.getUniqueId(), 
                "rstln", 
                Component.text("rstln"), 
                "yellow",
                0
        );
        Participant participant = new Participant(offlineParticipant, mockPlayer);
        
        Assertions.assertEquals(offlineParticipant, participant);
    }
    
    @Test
    void parkourParticipantEquals() {
        Participant participant = new Participant(mockPlayer, "yellow", 0);
        ParkourParticipant subParticipant = new ParkourParticipant(participant);
        
        Assertions.assertEquals(participant, subParticipant);
    }
    
    @Test
    void spleefParticipantEquals() {
        Participant participant = new Participant(mockPlayer, "yellow", 0);
        SpleefParticipant subParticipant = new SpleefParticipant(participant);
        
        Assertions.assertEquals(participant, subParticipant);
    }
    
    @Test
    void footRaceParticipantEquals() {
        Participant participant = new Participant(mockPlayer, "yellow", 0);
        FootRaceParticipant subParticipant = new FootRaceParticipant(participant, 0);
        
        Assertions.assertEquals(participant, subParticipant);
    }
    
    @Test
    void survivalGamesParticipantEquals() {
        Participant participant = new Participant(mockPlayer, "yellow", 0);
        SurvivalGamesParticipant subParticipant = new SurvivalGamesParticipant(participant);
        
        Assertions.assertEquals(participant, subParticipant);
    }
    
    @Test
    void ctfParticipantEquals() {
        Participant participant = new Participant(mockPlayer, "yellow", 0);
        CTFParticipant ctfParticipant = new CTFParticipant(participant);
        CTFMatchParticipant ctfMatchParticipant = new CTFMatchParticipant(ctfParticipant, CaptureTheFlagMatch.Affiliation.NORTH, true);
        
        Assertions.assertEquals(participant, ctfParticipant);
        Assertions.assertEquals(participant, ctfMatchParticipant);
        Assertions.assertEquals(ctfMatchParticipant, ctfParticipant);
    }
    
}