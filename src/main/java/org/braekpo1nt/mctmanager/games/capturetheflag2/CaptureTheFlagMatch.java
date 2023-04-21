package org.braekpo1nt.mctmanager.games.capturetheflag2;

import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.braekpo1nt.mctmanager.games.capturetheflag.Arena;
import org.braekpo1nt.mctmanager.games.capturetheflag.MatchPairing;
import org.braekpo1nt.mctmanager.games.utils.ParticipantInitializer;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;

import java.util.*;

public class CaptureTheFlagMatch {
    
    private final Main plugin;
    private final GameManager gameManager;
    private final MatchPairing matchPairing;
    private final Arena arena;
    private List<Player> northParticipants;
    private List<Player> southParticipants;
    private Map<UUID, Boolean> participantsAreAlive;
    private Map<UUID, Integer> killCount;
    
    public CaptureTheFlagMatch(Main plugin, GameManager gameManager, MatchPairing matchPairing, Arena arena) {
        this.plugin = plugin;
        this.gameManager = gameManager;
        this.matchPairing = matchPairing;
        this.arena = arena;
    }
    
    public MatchPairing getMatchPairing() {
        return matchPairing;
    }
    
    public void start(List<Player> newNorthParticipants, List<Player> newSouthParticipants) {
        northParticipants = new ArrayList<>();
        southParticipants = new ArrayList<>();
        participantsAreAlive = new HashMap<>();
        killCount = new HashMap<>();
        for (Player northParticipant : newNorthParticipants) {
            initializeParticipant(northParticipant, true);
        }
        for (Player southParticipant : newSouthParticipants) {
            initializeParticipant(southParticipant, false);
        }
    }
    
    private void initializeParticipant(Player participant, boolean north) {
        UUID participantUniqueId = participant.getUniqueId();
        participantsAreAlive.put(participantUniqueId, true);
        killCount.put(participantUniqueId, 0);
        if (north) {
            northParticipants.add(participant);
            participant.teleport(arena.northSpawn());
        } else {
            southParticipants.add(participant);
            participant.teleport(arena.northSpawn());
        }
        initializeFastBoard(participant);
        participant.getInventory().clear();
        participant.setGameMode(GameMode.ADVENTURE);
        ParticipantInitializer.clearStatusEffects(participant);
        ParticipantInitializer.resetHealthAndHunger(participant);
    }
    
    public void stop() {
        
    }
    
    private void initializeFastBoard(Player participant) {
        throw new UnsupportedOperationException("initialize fast board in match");
    }
    
}
