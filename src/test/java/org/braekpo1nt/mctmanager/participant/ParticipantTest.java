package org.braekpo1nt.mctmanager.participant;

import net.kyori.adventure.text.Component;
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
                "yellow"
        );
        Participant participant = new Participant(offlineParticipant, mockPlayer);
        
        Assertions.assertEquals(offlineParticipant, participant);
    }
    
    @Test
    void survivalGamesParticipantEquals() {
        Participant participant = new Participant(mockPlayer, "yellow");
        SurvivalGamesParticipant sgParticipant = new SurvivalGamesParticipant(participant);
        
        Assertions.assertEquals(participant, sgParticipant);
    }
    
}