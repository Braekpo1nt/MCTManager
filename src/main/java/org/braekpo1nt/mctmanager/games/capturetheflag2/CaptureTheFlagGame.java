package org.braekpo1nt.mctmanager.games.capturetheflag2;

import com.onarandombox.MultiverseCore.api.MVWorldManager;
import com.onarandombox.MultiverseCore.api.MultiverseWorld;
import com.onarandombox.MultiverseCore.utils.AnchorManager;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.braekpo1nt.mctmanager.games.capturetheflag.Arena;
import org.braekpo1nt.mctmanager.games.capturetheflag.MatchPairing;
import org.braekpo1nt.mctmanager.games.interfaces.MCTGame;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Capture the flag games are broken down into the following hierarchy:
 * - The entire game: Contains multiple rounds. Kicks off the rounds, and only ends when all rounds are over
 * - Rounds: A round of the game, contains multiple matches. Kicks off the matches, and only ends when all matches are done.
 * - Matches: a match of two teams in a specific arena. Handles kills, points, and respawns within that specific arena with those two teams and nothing else. Tells the round when it's over. 
 */
public class CaptureTheFlagGame implements MCTGame, Listener {
    
    private final GameManager gameManager;
    private final World captureTheFlagWorld;
    private final List<Arena> arenas;
    private final Location spawnObservatory;
    private int currentRoundIndex;
    private int maxRounds;
    private List<CaptureTheFlagRound> rounds;
    private List<Player> participants;
    
    
    public CaptureTheFlagGame(Main plugin, GameManager gameManager) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        this.gameManager = gameManager;
        MVWorldManager worldManager = Main.multiverseCore.getMVWorldManager();
        MultiverseWorld mvCaptureTheFlagWorld = worldManager.getMVWorld("FT");
        this.captureTheFlagWorld = mvCaptureTheFlagWorld.getCBWorld();
        AnchorManager anchorManager = Main.multiverseCore.getAnchorManager();
        spawnObservatory = anchorManager.getAnchorLocation("capture-the-flag");
        arenas = initializeArenas();
    }
    
    @Override
    public void start(List<Player> newParticipants) {
        List<String> teamNames = gameManager.getTeamNames(newParticipants);
        List<MatchPairing> matchPairings = generateMatchPairings(teamNames);
        rounds = generateRounds(matchPairings);
        currentRoundIndex = 0;
        maxRounds = rounds.size();
        participants = new ArrayList<>();
        for (Player participant : newParticipants) {
            initializeParticipant(participant);
        }
        startNextRound();
    }
    
    private void initializeParticipant(Player participant) {
        participants.add(participant);
    }
    
    private void startNextRound() {
        CaptureTheFlagRound nextRound = rounds.get(currentRoundIndex);
        nextRound.start(participants);
    }
    
    /**
     * Generates all combinations of size 2 of the given teams in the form of {@link MatchPairing}s 
     * @param teamNames The teams to generate the combinations of
     * @return A list of at least two MatchPairing objects representing all combinations of size 2
     * of the given list of team names
     * @throws IndexOutOfBoundsException if teamNames.size() is 1 or less
     */
    public static @NotNull List<MatchPairing> generateMatchPairings(@NotNull List<String> teamNames) {
        List<MatchPairing> combinations = new ArrayList<>();
        for (int i = 0; i < teamNames.size(); i++) {
            for (int j = i + 1; j < teamNames.size(); j++) {
                String northTeam = teamNames.get(i);
                String southTeam = teamNames.get(j);
                MatchPairing pairing = new MatchPairing(northTeam, southTeam);
                combinations.add(pairing);
            }
        }
        return combinations;
    }
    
    /**
     * Given n {@link MatchPairing}s, where x is the number of arenas in {@link CaptureTheFlagGame#arenas}
     * if n>=x, returns ceiling(n/x) rounds. Each round should hold between 1 and x matches.
     * if n<x, returns 1 round with n matches.
     * Note: If ceiling(n/x) is not a multiple of x, the last round in the list will hold the remainder of n/x (between 1 and x-1) {@link CaptureTheFlagMatch}s so that all matches are accounted for.
     * @param matchPairings The match parings to create the rounds for
     * @return A list of {@link CaptureTheFlagRound}s containing n {@link CaptureTheFlagMatch}s between them, where n is the number of given {@link MatchPairing}s
     */
    public @NotNull List<CaptureTheFlagRound> generateRounds(@NotNull List<MatchPairing> matchPairings) {
        int numberOfRounds = (matchPairings.size() / arenas.size()) + (matchPairings.size() % arenas.size());
        int numberOfMatchesPerRound = arenas.size();
        List<CaptureTheFlagRound> rounds = new ArrayList<>(numberOfRounds);
        Iterator<MatchPairing> iterator = matchPairings.iterator();
        for (int i = 0; i < numberOfRounds; i++) {
            List<CaptureTheFlagMatch> roundMatches = new ArrayList<>(numberOfMatchesPerRound);
            int matchCount = 0;
            while(iterator.hasNext() && matchCount < numberOfMatchesPerRound) {
                MatchPairing matchPairing = iterator.next();
                CaptureTheFlagMatch roundMatch = new CaptureTheFlagMatch(matchPairing, arenas.get(matchCount));
                roundMatches.add(roundMatch);
                matchCount++;
            }
            CaptureTheFlagRound newRound = new CaptureTheFlagRound(roundMatches, spawnObservatory);
            rounds.add(newRound);
        }
        return rounds;
    }
    
    
    
    @Override
    public void stop() {
        
    }
    
    @Override
    public void onParticipantJoin(Player participant) {
        
    }
    
    @Override
    public void onParticipantQuit(Player participant) {
        
    }
    
    private List<Arena> initializeArenas() {
        List<Arena> newArenas = new ArrayList<>(4);
        //NorthWest
        newArenas.add(new Arena(
                new Location(captureTheFlagWorld, -15, -16, -1043), // North spawn
                new Location(captureTheFlagWorld, -15, -16, -1003), // South spawn
                new Location(captureTheFlagWorld, -6, -13, -1040), // North flag 
                new Location(captureTheFlagWorld, -24, -13, -1006), // South flag 
                new Location(captureTheFlagWorld, -17, -16, -1042), // North barrier
                new Location(captureTheFlagWorld, -17, -16, -1004) // South barrier 
        
        ));
        //NorthEast
        newArenas.add(new Arena(
                new Location(captureTheFlagWorld, 15, -16, -1043), // North spawn
                new Location(captureTheFlagWorld, 15, -16, -1003), // South spawn
                new Location(captureTheFlagWorld, 24, -13, -1040), // North flag 
                new Location(captureTheFlagWorld, 6, -13, -1006), // South flag 
                new Location(captureTheFlagWorld, 13, -16, -1042), // North barrier
                new Location(captureTheFlagWorld, 13, -16, -1004) // South barrier 
        ));
        //SouthWest
        newArenas.add(new Arena(
                new Location(captureTheFlagWorld, -15, -16, -997), // North spawn
                new Location(captureTheFlagWorld, -15, -16, -957), // South spawn
                new Location(captureTheFlagWorld, -6, -13, -994), // North flag 
                new Location(captureTheFlagWorld, -24, -13, -960), // South flag 
                new Location(captureTheFlagWorld, -17, -16, -996), // North barrier
                new Location(captureTheFlagWorld, -17, -16, -958) // South barrier 
        ));
        //SouthEast
        newArenas.add(new Arena(
                new Location(captureTheFlagWorld, 15, -16, -997), // North spawn
                new Location(captureTheFlagWorld, 15, -16, -957), // South spawn
                new Location(captureTheFlagWorld, 24, -13, -994), // North flag 
                new Location(captureTheFlagWorld, 6, -13, -960), // South flag 
                new Location(captureTheFlagWorld, 13, -16, -996), // North barrier
                new Location(captureTheFlagWorld, 13, -16, -958) // South barrier 
        ));
        
        return newArenas;
    }
}
