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

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ParticipantTest {
    
    static Participant participant;
    static Player mockPlayer;
    
    @BeforeAll
    static void setup() {
        mockPlayer = mock(Player.class);
        when(mockPlayer.getName()).thenReturn("rstln");
        when(mockPlayer.getUniqueId()).thenReturn(UUID.randomUUID());
        OfflineParticipant offlineParticipant = new OfflineParticipant(
                mockPlayer.getUniqueId(),
                "rstln",
                Component.text("rstln"),
                "yellow",
                0
        );
        participant = new Participant(offlineParticipant, mockPlayer);
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
        
        Assertions.assertEquals(offlineParticipant, participant);
    }
    
    @Test
    void parkourParticipantEquals() {
        ParkourParticipant subParticipant = new ParkourParticipant(participant, 0);
        
        Assertions.assertEquals(participant, subParticipant);
    }
    
    @Test
    void spleefParticipantEquals() {
        SpleefParticipant subParticipant = new SpleefParticipant(participant, 0);
        
        Assertions.assertEquals(participant, subParticipant);
    }
    
    @Test
    void footRaceParticipantEquals() {
        FootRaceParticipant subParticipant = new FootRaceParticipant(participant, 0, 0);
        
        Assertions.assertEquals(participant, subParticipant);
    }
    
    @Test
    void survivalGamesParticipantEquals() {
        SurvivalGamesParticipant subParticipant = new SurvivalGamesParticipant(participant, 0);
        
        Assertions.assertEquals(participant, subParticipant);
    }
    
    @Test
    void ctfParticipantEquals() {
        CTFParticipant ctfParticipant = new CTFParticipant(participant);
        CTFMatchParticipant ctfMatchParticipant = new CTFMatchParticipant(ctfParticipant, CaptureTheFlagMatch.Affiliation.NORTH, true);
        
        Assertions.assertEquals(participant, ctfParticipant);
        Assertions.assertEquals(participant, ctfMatchParticipant);
        Assertions.assertEquals(ctfMatchParticipant, ctfParticipant);
    }
    
    @Test
    void participantID() {
        ParticipantID participantID = participant.getParticipantID();
        ParticipantID identical = new ParticipantID(participant.getUniqueId(), "yellow");
        ParticipantID differentUUID = new ParticipantID(UUID.randomUUID(), "yellow");
        ParticipantID differentTeam = new ParticipantID(participant.getUniqueId(), "red");
        
        Assertions.assertEquals(participantID, identical);
        Assertions.assertNotEquals(participantID, differentUUID);
        Assertions.assertNotEquals(participantID, differentTeam);
        
        Map<ParticipantID, Integer> map = new HashMap<>();
        map.put(participantID, 1);
        map.put(identical, 2);
        
        Assertions.assertEquals(map.get(participantID), 2);
        
        map.put(differentUUID, 3);
        Assertions.assertEquals(map.get(participantID), 2);
        Assertions.assertEquals(map.get(differentUUID), 3);
    }
    
}