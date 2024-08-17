package org.braekpo1nt.mctmanager.games.game.capturetheflag.match;

import lombok.Data;
import org.braekpo1nt.mctmanager.games.game.capturetheflag.Arena;
import org.braekpo1nt.mctmanager.games.game.capturetheflag.CaptureTheFlagGame;
import org.braekpo1nt.mctmanager.games.game.capturetheflag.MatchPairing;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.function.Consumer;

@Data
public class CaptureTheFlagMatch {
    
    
    private final CaptureTheFlagGame parentContext;
    /**
     * To be called when this match is over
     */
    private final Consumer<CaptureTheFlagMatch> matchIsOver;
    private final MatchPairing matchPairing;
    private final Arena arena;
    
    public CaptureTheFlagMatch(CaptureTheFlagGame parentContext, Consumer<CaptureTheFlagMatch> matchIsOver, MatchPairing matchPairing, Arena arena) {
        this.parentContext = parentContext;
        this.matchIsOver = matchIsOver;
        this.matchPairing = matchPairing;
        this.arena = arena;
    }
    
    public void start(List<Player> northParticipants, List<Player> southParticipants) {
        throw new UnsupportedOperationException("not yet implemented");
    }
    
}
