package org.braekpo1nt.mctmanager.games.capturetheflag2;

import org.braekpo1nt.mctmanager.games.capturetheflag.Arena;
import org.braekpo1nt.mctmanager.games.capturetheflag.MatchPairing;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class CaptureTheFlagMatch {
    
    private final MatchPairing matchPairing;
    private final Arena arena;
    private List<Player> northParticipants;
    private List<Player> southParticipants;
    
    public CaptureTheFlagMatch(MatchPairing matchPairing, Arena arena) {
        this.matchPairing = matchPairing;
        this.arena = arena;
    }
    
    public MatchPairing getMatchPairing() {
        return matchPairing;
    }
    
    public void start(List<Player> newNorthParticipants, List<Player> newSouthParticipants) {
        northParticipants = new ArrayList<>();
        southParticipants = new ArrayList<>();
        for (Player northParticipant : newNorthParticipants) {
            initializeNorthParticipant(northParticipant);
        }
        for (Player southParticipant : newSouthParticipants) {
            initializeSouthParticipant(southParticipant);
        }
    }
    
    private void initializeNorthParticipant(Player northParticipant) {
        
    }
    
    private void initializeSouthParticipant(Player southParticipant) {
        
    }
    
    public void stop() {
        
    }
    
}
