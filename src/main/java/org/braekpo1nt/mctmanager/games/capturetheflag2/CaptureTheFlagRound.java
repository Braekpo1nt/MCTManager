package org.braekpo1nt.mctmanager.games.capturetheflag2;

import org.braekpo1nt.mctmanager.games.utils.ParticipantInitializer;
import org.braekpo1nt.mctmanager.ui.FastBoardManager;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * A round is made up of multiple matches. It kicks off the matches it contains, and ends
 * when all the matches are over.
 */
public class CaptureTheFlagRound {
    
    private final FastBoardManager fastBoardManager;
    private final List<CaptureTheFlagMatch> matches;
    private List<Player> participants;
    private final Location spawnObservatory;
    
    public CaptureTheFlagRound(FastBoardManager fastBoardManager, List<CaptureTheFlagMatch> matches, Location spawnObservatory) {
        this.fastBoardManager = fastBoardManager;
        this.matches = matches;
        this.spawnObservatory = spawnObservatory;
    }
    
    public void start(List<Player> newParticipants) {
        participants = new ArrayList<>();
        for (Player participant : newParticipants) {
            initializeParticipant(participant);
        }
    }
    
    private void initializeParticipant(Player participant) {
        participants.add(participant);
        initializeFastBoard(participant);
        participant.teleport(spawnObservatory);
        participant.getInventory().clear();
        participant.setGameMode(GameMode.ADVENTURE);
        ParticipantInitializer.clearStatusEffects(participant);
        ParticipantInitializer.resetHealthAndHunger(participant);
    }
    
    private void initializeFastBoard(Player participant) {
        fastBoardManager.updateLine(
                participant.getUniqueId(),
                4,
                "Starting in:"
        );
        fastBoardManager.updateLine(
                participant.getUniqueId(),
                5,
                "0"
        );
    }
    
    public void stop() {
        
    }
    
    public List<CaptureTheFlagMatch> getMatches() {
        return matches;
    }
}
