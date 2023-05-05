package org.braekpo1nt.mctmanager.games.capturetheflag;

import com.onarandombox.MultiverseCore.api.MVWorldManager;
import com.onarandombox.MultiverseCore.api.MultiverseWorld;
import com.onarandombox.MultiverseCore.utils.AnchorManager;
import net.kyori.adventure.text.*;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.braekpo1nt.mctmanager.games.interfaces.MCTGame;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
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
        HandlerList.unregisterAll(this);
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
    
    private void resetParticipant(Player participant) {
        participant.getInventory().clear();
        hideFastBoard(participant);
    }
    
    @Override
    public void onParticipantJoin(Player participant) {
        if (currentRoundIndex < 0) {
            initializeParticipant(participant);
            return;
        }
        // TODO: if the joining player is on a team that is not currently in the list of rounds/matches, we must add its matches to the lineup. If the team is one that completely left, and is now rejoining, we must check for its matches that have already been played out and make sure they aren't duplicated in the new lineup. 
        String teamName = gameManager.getTeamName(participant.getUniqueId());
    }
    
    @Override
    public void onParticipantQuit(Player participant) {
        if (currentRoundIndex >= 0) {
            CaptureTheFlagRound currentRound = rounds.get(currentRoundIndex);
            currentRound.onParticipantQuit(participant);
        }
        resetParticipant(participant);
        participants.remove(participant);
    
        // TODO: if an entire team has left, remove it's future matches. Store its old matches so that if they rejoin, they can be re-incorporated into the lineup without duplicate matches being played again.
        String teamName = gameManager.getTeamName(participant.getUniqueId());
        if (getTeamsNextRoundIndex(teamName) == -1) {
            return;
        }
        if (!entireTeamHasQuit(teamName)) {
            return;
        }
        // the entire team has quit, and they have rounds left. Those rounds must be removed.
        removeFutureMatchesForTeam(teamName);
    }
    
    private void removeFutureMatchesForTeam(String teamName) {
        List<String> onlineTeamNames = gameManager.getTeamNames(participants);
        List<MatchPairing> newMatchPairings = CaptureTheFlagUtils.generateMatchPairings(onlineTeamNames);
        // remove already completed or in progress match pairings from newMatchPairings
        for (int i = 0; i <= currentRoundIndex; i++) {
            CaptureTheFlagRound round = rounds.get(i);
            List<CaptureTheFlagMatch> roundMatches = round.getMatches();
            for (CaptureTheFlagMatch roundMatch : roundMatches) {
                MatchPairing roundMatchPairing = roundMatch.getMatchPairing();
                newMatchPairings.remove(roundMatchPairing);
            }
        }
        // generate a new set of rounds with the newMatchPairings, with the first and current round being the current round, if there is a currently active round
        List<CaptureTheFlagRound> newRounds = generateRounds(newMatchPairings);
        if (currentRoundIndex >= 0) {
            CaptureTheFlagRound currentRound = rounds.get(currentRoundIndex);
            newRounds.add(0, currentRound);
        }
        currentRoundIndex = 0;
        for (Player participant : participants) {
            throw new UnsupportedOperationException();
        }
    }
    
    /**
     * Check if the entire team has quit (no participants exist with the given team)
     * @param teamName The team name to check for
     * @return True if there are no participants on the given team. False otherwise.
     */
    private boolean entireTeamHasQuit(String teamName) {
        for (Player participant : participants) {
            String participantTeamName = gameManager.getTeamName(participant.getUniqueId());
            if (participantTeamName.equals(teamName)) {
                return false;
            }
        }
        return true;
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
    
    private void startNextRound() {
        CaptureTheFlagRound nextRound = rounds.get(currentRoundIndex);
        List<Player> roundParticipants = new ArrayList<>();
        List<Player> onDeckParticipants = new ArrayList<>();
        for (Player participant : participants) {
            String teamName = gameManager.getTeamName(participant.getUniqueId());
            Component teamDisplayName = gameManager.getFormattedTeamDisplayName(teamName);
            String oppositeTeam = nextRound.getOppositeTeam(teamName);
            if (oppositeTeam != null) {
                roundParticipants.add(participant);
                Component oppositeTeamDisplayName = gameManager.getFormattedTeamDisplayName(oppositeTeam);
                participant.sendMessage(Component.empty()
                        .append(teamDisplayName)
                        .append(Component.text(" is competing against "))
                        .append(oppositeTeamDisplayName)
                        .append(Component.text(" this round.")));
            } else {
                onDeckParticipants.add(participant);
                int participantsNextRoundIndex = getTeamsNextRoundIndex(teamName);
                if (participantsNextRoundIndex < 0) {
                    participant.sendMessage(Component.empty()
                            .append(teamDisplayName)
                            .append(Component.text(" has no more rounds. They've competed against every team.")));
                } else {
                    participant.sendMessage(Component.empty()
                            .append(teamDisplayName)
                            .append(Component.text(" is not competing in this round. Their next round is "))
                            .append(Component.text(participantsNextRoundIndex)));
                }
            }
        }
        nextRound.start(roundParticipants, onDeckParticipants);
    }

    /**
     * Gets the index of the next round the given team is in, if they are in a successive round.
     * @param teamName The teamName to search for
     * @return The index of the next round the given team is in, -1 if they are not in any of the next rounds.
     */
    private int getTeamsNextRoundIndex(@NotNull String teamName) {
        for (int i = currentRoundIndex + 1; i < rounds.size(); i++) {
            CaptureTheFlagRound round = rounds.get(i);
            String oppositeTeam = round.getOppositeTeam(teamName);
            if (oppositeTeam != null) {
                return i;
            }
        }
        return -1;
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
            CaptureTheFlagRound newRound = new CaptureTheFlagRound(this, plugin, gameManager, spawnObservatory, captureTheFlagWorld);
            newRound.createMatches(roundMatchPairings, arenas.subList(0, roundMatchPairings.size()));
            rounds.add(newRound);
        }
        return rounds;
    }
    
    @EventHandler
    public void onPlayerDamage(EntityDamageEvent event) {
        if (!gameActive) {
            return;
        }
        if (event.getCause().equals(EntityDamageEvent.DamageCause.VOID)) {
            return;
        }
        if (!(event.getEntity() instanceof Player participant)) {
            return;
        }
        if (!participants.contains(participant)) {
            return;
        }
        if (currentRoundIndex >= rounds.size()) {
            return;
        }
        if (rounds.get(currentRoundIndex).isAliveInMatch(participant)) {
            return;
        }
        event.setCancelled(true);
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
                "",
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
                new Location(captureTheFlagWorld, -17, -16, -1004), // South barrier 
                new BoundingBox(-26, -17, -1001, -4, 4, -1045)
        ));
        //NorthEast
        newArenas.add(new Arena(
                new Location(captureTheFlagWorld, 15, -16, -1043), // North spawn
                new Location(captureTheFlagWorld, 15, -16, -1003), // South spawn
                new Location(captureTheFlagWorld, 24, -13, -1040), // North flag 
                new Location(captureTheFlagWorld, 6, -13, -1006), // South flag 
                new Location(captureTheFlagWorld, 13, -16, -1042), // North barrier
                new Location(captureTheFlagWorld, 13, -16, -1004), // South barrier
                new BoundingBox(4, 4, -1045, 26, -17, -1001)
        ));
        //SouthWest
        newArenas.add(new Arena(
                new Location(captureTheFlagWorld, -15, -16, -997), // North spawn
                new Location(captureTheFlagWorld, -15, -16, -957), // South spawn
                new Location(captureTheFlagWorld, -6, -13, -994), // North flag 
                new Location(captureTheFlagWorld, -24, -13, -960), // South flag 
                new Location(captureTheFlagWorld, -17, -16, -996), // North barrier
                new Location(captureTheFlagWorld, -17, -16, -958), // South barrier 
                new BoundingBox(-4, -17, -999, -26, 4, -955)
        ));
        //SouthEast
        newArenas.add(new Arena(
                new Location(captureTheFlagWorld, 15, -16, -997), // North spawn
                new Location(captureTheFlagWorld, 15, -16, -957), // South spawn
                new Location(captureTheFlagWorld, 24, -13, -994), // North flag 
                new Location(captureTheFlagWorld, 6, -13, -960), // South flag 
                new Location(captureTheFlagWorld, 13, -16, -996), // North barrier
                new Location(captureTheFlagWorld, 13, -16, -958), // South barrier 
                new BoundingBox(4, 4, -955, 26, -17, -999)
        ));
        
        return newArenas;
    }
    
    /**
     * Messages all the participants of the game (whether they're in a match or not)
     * @param message The message to send
     */
    public void messageAllParticipants(Component message) {
        for (Player participant : participants) {
            participant.sendMessage(message);
        }
    }
    
    // Testing methods
    /**
     * Returns a copy of the list of participants. Not the actual list, modifying the return value
     * of this function does not modify the actual list of participants.
     * @return A copy of the list of participants
     */
    public List<Player> getParticipants() {
        return new ArrayList<>(participants);
    }
    
    public @Nullable CaptureTheFlagRound getCurrentRound() {
        if (currentRoundIndex < 0) {
            return null;
        }
        return rounds.get(currentRoundIndex);
    }
    
    /**
     * @return a copy of the rounds list
     */
    public List<CaptureTheFlagRound> getRounds() {
        return new ArrayList<>(rounds);
    }
}
