package org.braekpo1nt.mctmanager.games.capturetheflag;

import com.onarandombox.MultiverseCore.api.MVWorldManager;
import com.onarandombox.MultiverseCore.api.MultiverseWorld;
import com.onarandombox.MultiverseCore.utils.AnchorManager;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.braekpo1nt.mctmanager.games.interfaces.MCTGame;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Capture the flag games are broken down into the following hierarchy:
 * - The entire game: Contains multiple rounds. Kicks off the rounds, and only ends when all rounds are over
 * - Rounds: A round of the game, contains multiple matches. Kicks off the matches, and only ends when all matches are done.
 * - Matches: a match of two teams in a specific arena. Handles kills, points, and respawns within that specific arena with those two teams and nothing else. Tells the round when it's over. 
 */
public class CaptureTheFlagGame implements MCTGame, Listener {
    
    private final Main plugin;
    private final GameManager gameManager;
    private final World captureTheFlagWorld;
    private final List<Arena> arenas;
    private final Location spawnObservatory;
    private int currentRoundIndex;
    private int maxRounds;
    private List<CaptureTheFlagRound> rounds;
    private final String title = ChatColor.BLUE+"Capture the Flag";
    private List<Player> participants;
    private boolean gameActive = false;
    
    public CaptureTheFlagGame(Main plugin, GameManager gameManager) {
        this.plugin = plugin;
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
        List<MatchPairing> matchPairings = CaptureTheFlagUtils.generateMatchPairings(teamNames);
        rounds = generateRounds(matchPairings);
        currentRoundIndex = 0;
        maxRounds = rounds.size();
        participants = new ArrayList<>();
        for (Player participant : newParticipants) {
            initializeParticipant(participant);
        }
        gameActive = true;
        startNextRound();
        Bukkit.getLogger().info("Starting Capture the Flag");
    }
    
    private void initializeParticipant(Player participant) {
        participants.add(participant);
        initializeFastBoard(participant);
    }
    
    @Override
    public void stop() {
        CaptureTheFlagRound currentRound = rounds.get(currentRoundIndex);
        currentRound.stop();
        rounds.clear();
        gameActive = false;
        for (Player participant : participants) {
            resetParticipant(participant);
        }
        participants.clear();
        gameManager.gameIsOver();
        Bukkit.getLogger().info("Stopping Capture the Flag");
    }

    /**
     * Tells the game that the current round is over. If there are no rounds left, ends the game. If there are rounds left, starts the next round.
     */
    public void roundIsOver() {
        if (allRoundsAreOver()) {
            stop();
            return;
        }
        currentRoundIndex++;
        startNextRound();
    }

    /**
     * Checks if all rounds are over
     * @return true if all rounds are over, i.e. there is no next round, false otherwise
     */
    private boolean allRoundsAreOver() {
        return rounds.size() >= currentRoundIndex + 1;
    }

    private void resetParticipant(Player participant) {
        participant.getInventory().clear();
        hideFastBoard(participant);
    }
    
    private void startNextRound() {
        CaptureTheFlagRound nextRound = rounds.get(currentRoundIndex);
        nextRound.start(participants);
    }
    
    /**
     * Given n {@link MatchPairing}s, where x is the number of arenas in {@link CaptureTheFlagGame#arenas}
     * if n>=x, returns ceiling(n/x) rounds. Each round should hold between 1 and x matches.
     * if n<x, returns 1 round with n matches.
     * Note: If ceiling(n/x) is not a multiple of x, the last round in the list will hold the remainder of n/x (between 1 and x-1) {@link CaptureTheFlagMatch}s so that all matches are accounted for.
     * @param matchPairings The match parings to create the rounds for
     * @return A list of {@link CaptureTheFlagRound}s containing n {@link CaptureTheFlagMatch}s between them, where n is the number of given {@link MatchPairing}s
     */
    private @NotNull List<CaptureTheFlagRound> generateRounds(@NotNull List<MatchPairing> matchPairings) {
        List<CaptureTheFlagRound> rounds = new ArrayList<>();
        List<List<MatchPairing>> roundMatchPairingsList = CaptureTheFlagUtils.generateRoundMatchPairings(matchPairings, arenas.size());
        for (List<MatchPairing> roundMatchPairings : roundMatchPairingsList) {
            CaptureTheFlagRound newRound = new CaptureTheFlagRound(this, plugin, gameManager, spawnObservatory);
            newRound.createMatches(roundMatchPairings, arenas.subList(0, roundMatchPairings.size()));
        }
        return rounds;
    }
    
    @Override
    public void onParticipantJoin(Player participant) {
        
    }
    
    @Override
    public void onParticipantQuit(Player participant) {
        
    }
    
    private void initializeFastBoard(Player participant) {
        gameManager.getFastBoardManager().updateLines(
                participant.getUniqueId(),
                title,
                "", // current enemy team
                String.format("Round %d/%d", currentRoundIndex+1, maxRounds), //current round
                "",
                "", // timer name
                "", // timer
                ""
        );
    }
    
    private void hideFastBoard(Player participant) {
        gameManager.getFastBoardManager().updateLines(
                participant.getUniqueId()
        );
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
