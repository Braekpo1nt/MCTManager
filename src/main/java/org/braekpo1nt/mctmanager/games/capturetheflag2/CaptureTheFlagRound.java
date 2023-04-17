package org.braekpo1nt.mctmanager.games.capturetheflag2;

import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.bukkit.event.Listener;

import java.util.List;

/**
 * A round is made up of multiple matches. It kicks off the matches it contains, and ends
 * when all the matches are over.
 */
public class CaptureTheFlagRound {
    
    private final List<CaptureTheFlagMatch> matches;
    
    public CaptureTheFlagRound(List<CaptureTheFlagMatch> matches) {
        this.matches = matches;
    }
    
    public void start() {
        
    }
    
    public void stop() {
        
    }
    
}
