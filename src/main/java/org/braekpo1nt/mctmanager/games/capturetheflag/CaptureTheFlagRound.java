package org.braekpo1nt.mctmanager.games.capturetheflag;

import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.braekpo1nt.mctmanager.games.utils.ParticipantInitializer;
import org.braekpo1nt.mctmanager.ui.TimeStringUtils;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * A round is made up of multiple matches. It kicks off the matches it contains, and ends
 * when all the matches are over.
 */
public class CaptureTheFlagRound {
    
    private final CaptureTheFlagGame captureTheFlagGame;
    private final Main plugin;
    private final GameManager gameManager;
    private List<CaptureTheFlagMatch> matches;
    private List<Player> participants = new ArrayList<>();
    private List<Player> onDeckParticipants;
    private final Location spawnObservatory;
    private int matchesStartingCountDownTaskId;
    private final World captureTheFlagWorld;

    public CaptureTheFlagRound(CaptureTheFlagGame captureTheFlagGame, Main plugin, GameManager gameManager, 
                               Location spawnObservatory, World captureTheFlagWorld) {
        this.captureTheFlagGame = captureTheFlagGame;
        this.plugin = plugin;
        this.gameManager = gameManager;
        this.spawnObservatory = spawnObservatory;
        this.captureTheFlagWorld = captureTheFlagWorld;
    }
    
    /**
     * Creates the matches for this round from the provided matchPairings and arenas. matchPairings.size() 
     * must be less than or equal to arenas.size(), or you will get null pointer exceptions. 
     * @param matchPairings The MatchPairings to create {@link CaptureTheFlagMatch}s from
     * @param arenas The arenas to assign to each {@link CaptureTheFlagMatch}
     * @throws NullPointerException if matchPairings.size() is greater than arenas.size()
     */
    public void createMatches(List<MatchPairing> matchPairings, List<Arena> arenas) {
        matches = new ArrayList<>();
        for (int i = 0; i < matchPairings.size(); i++) {
            MatchPairing matchPairing = matchPairings.get(i);
            Arena arena = arenas.get(i);
            CaptureTheFlagMatch match = new CaptureTheFlagMatch(this, plugin, gameManager, 
                    matchPairing, arena, spawnObservatory, captureTheFlagWorld);
            matches.add(match);
        }
    }
    
    public void start(List<Player> newParticipants, List<Player> newOnDeckParticipants) {
        participants = new ArrayList<>();
        onDeckParticipants = new ArrayList<>();
        for (Player participant : newParticipants) {
            initializeParticipant(participant);
        }
        for (Player onDeck : newOnDeckParticipants) {
            initializeOnDeckParticipant(onDeck);
        }
        startMatchesStartingCountDown();
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
    
    private void initializeOnDeckParticipant(Player onDeckParticipant) {
        onDeckParticipants.add(onDeckParticipant);
        initializeOndDeckFastBoard(onDeckParticipant);
        onDeckParticipant.teleport(spawnObservatory);
        onDeckParticipant.getInventory().clear();
        onDeckParticipant.setGameMode(GameMode.ADVENTURE);
        ParticipantInitializer.clearStatusEffects(onDeckParticipant);
        ParticipantInitializer.resetHealthAndHunger(onDeckParticipant);
    }
    
    private void roundIsOver() {
        stop();
        captureTheFlagGame.roundIsOver();
    }
    public void stop() {
        cancelAllTasks();
        for (CaptureTheFlagMatch match : matches) {
            match.stop();
        }
        for (Player participant : participants) {
            resetParticipant(participant);
        }
        for (Player onDeckParticipant : onDeckParticipants) {
            resetOnDeckParticipant(onDeckParticipant);
        }
        participants.clear();
    }
    
    private void resetParticipant(Player participant) {
        participant.getInventory().clear();
        participant.setGameMode(GameMode.ADVENTURE);
        ParticipantInitializer.clearStatusEffects(participant);
        ParticipantInitializer.resetHealthAndHunger(participant);
    }
    
    private void resetOnDeckParticipant(Player onDeckParticipant) {
        onDeckParticipant.getInventory().clear();
        onDeckParticipant.setGameMode(GameMode.ADVENTURE);
        ParticipantInitializer.clearStatusEffects(onDeckParticipant);
        ParticipantInitializer.resetHealthAndHunger(onDeckParticipant);
    }
    
    public void onParticipantJoin(Player participant) {
        String teamName = gameManager.getTeamName(participant.getUniqueId());
        CaptureTheFlagMatch match = getMatch(teamName);
        if (match == null) {
            initializeOnDeckParticipant(participant);
            return;
        }
        if (!match.isActive()) {
            initializeParticipant(participant);
            return;
        }
        if (!match.isInClassSelection()) {
            initializeOnDeckParticipant(participant);
            return;
        }
        initializeParticipant(participant);
        match.onParticipantJoin(participant);
    }
    
    public void onParticipantQuit(Player participant) {
        if (onDeckParticipants.contains(participant)) {
            onOnDeckParticipantQuit(participant);
            return;
        }
        if (!participants.contains(participant)) {
            return;
        }
        String teamName = gameManager.getTeamName(participant.getUniqueId());
        CaptureTheFlagMatch match = getMatch(teamName);
        if (match == null) {
            resetParticipant(participant);
            participants.remove(participant);
            return;
        }
        match.onParticipantQuit(participant);
        resetParticipant(participant);
        participants.remove(participant);
    }
    
    /**
     * Code to handle when an on-deck participant leaves
     * @param onDeckParticipant the on-deck participant
     */
    private void onOnDeckParticipantQuit(Player onDeckParticipant) {
        resetOnDeckParticipant(onDeckParticipant);
        onDeckParticipants.remove(onDeckParticipant);
    }
    
    /**
     * Tells the round that the given match is over. If all matches are over, stops the round. If not all matches are over, teleports the players who were in the passed-in match to the spawn observatory.
     * @param match The match that is over. Must be one of the matches in {@link CaptureTheFlagRound#matches}.
     */
    public void matchIsOver(CaptureTheFlagMatch match) {
        if (allMatchesAreOver()) {
            roundIsOver();
            return;
        }
        MatchPairing matchPairing = match.getMatchPairing();
        for (Player participant : participants) {
            String team = gameManager.getTeamName(participant.getUniqueId());
            if (matchPairing.containsTeam(team)) {
                participant.teleport(spawnObservatory);
            }
        }
    }
    
    /**
     * Check if all the matches are over
     * @return True if all matches are over, false if even one match isn't over
     */
    private boolean allMatchesAreOver() {
        for (CaptureTheFlagMatch match : matches) {
            if (match.isActive()) {
                return false;
            }
        }
        return true;
    }
    
    private void startMatchesStartingCountDown() {
        this.matchesStartingCountDownTaskId = new BukkitRunnable() {
            int count = 10;
            @Override
            public void run() {
                if (count <= 0) {
                    startMatches();
                    this.cancel();
                    return;
                }
                displayMatchesStartingCountDown(count);
                count--;
            }
        }.runTaskTimer(plugin, 0L, 20L).getTaskId();
    }

    /**
     * Starts the matches that are in this round
     */
    private void startMatches() {
        for (CaptureTheFlagMatch match : matches) {
            MatchPairing matchPairing = match.getMatchPairing();
            List<Player> northParticipants = getParticipantsOnTeam(matchPairing.northTeam());
            List<Player> southParticipants = getParticipantsOnTeam(matchPairing.southTeam());
            match.start(northParticipants, southParticipants);
        }
    }

    private List<Player> getParticipantsOnTeam(String teamName) {
        List<Player> onTeam = new ArrayList<>();
        for (Player participant : participants) {
            String participantTeam = gameManager.getTeamName(participant.getUniqueId());
            if (participantTeam.equals(teamName)) {
                onTeam.add(participant);
            }
        }
        return onTeam;
    }

    private void cancelAllTasks() {
        Bukkit.getScheduler().cancelTask(matchesStartingCountDownTaskId);
    }
    
    /**
     * Displays the given seconds left before the matches start to the user via the FastBoards
     * @param secondsLeft The seconds left before the matches start
     */
    private void displayMatchesStartingCountDown(int secondsLeft) {
        String timeString = TimeStringUtils.getTimeString(secondsLeft);
        for (Player participant : participants) {
            gameManager.getFastBoardManager().updateLine(
                    participant.getUniqueId(),
                    5,
                    timeString
            );
        }
        for (Player participant : onDeckParticipants) {
            gameManager.getFastBoardManager().updateLine(
                    participant.getUniqueId(),
                    5,
                    timeString
            );
        }
    }
    
    private void initializeFastBoard(Player participant) {
        String teamName = gameManager.getTeamName(participant.getUniqueId());
        String enemyTeam = getOppositeTeam(teamName);
        ChatColor enemyColor = gameManager.getTeamChatColor(enemyTeam);
        String enemyDisplayName = gameManager.getTeamDisplayName(enemyTeam);
        gameManager.getFastBoardManager().updateLine(
                participant.getUniqueId(),
                1,
                "vs: "+ChatColor.BOLD+enemyColor+enemyDisplayName
        );
        gameManager.getFastBoardManager().updateLine(
                participant.getUniqueId(),
                4,
                "Starting in:"
        );
        gameManager.getFastBoardManager().updateLine(
                participant.getUniqueId(),
                5,
                "0"
        );
    }
    
    private void initializeOndDeckFastBoard(Player participant) {
        gameManager.getFastBoardManager().updateLine(
                participant.getUniqueId(),
                1,
                "On Deck"
        );
        gameManager.getFastBoardManager().updateLine(
                participant.getUniqueId(),
                4,
                "Starting in:"
        );
        gameManager.getFastBoardManager().updateLine(
                participant.getUniqueId(),
                5,
                "0"
        );
    }
    
    /**
     * Messages all participants of the entire game, whether they are in this round or not
     * @param message The message to send
     */
    public void messageAllGameParticipants(Component message) {
        captureTheFlagGame.messageAllParticipants(message);
    }
    
    /**
     * Checks if the participant is alive and in a match
     * @param participant The participant
     * @return True if the participant is alive and in a match, false otherwise
     */
    public boolean isAliveInMatch(Player participant) {
        for (CaptureTheFlagMatch match : matches) {
            if (match.isActive()) {
                if (match.isAliveInMatch(participant)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Gets the opposite team of the given teamName, if the teamName is contained in one of this round's
     * {@link CaptureTheFlagRound#matches}.
     * @param teamName The teamName to check for
     * @return The opposite team in the {@link CaptureTheFlagMatch} which contains the given teamName. Null if
     * the given teamName is not contained in any of the matches for this round.
     */
    public @Nullable String getOppositeTeam(@NotNull String teamName) {
        for (CaptureTheFlagMatch match : matches) {
            String oppositeTeam = match.getMatchPairing().oppositeTeam(teamName);
            if (oppositeTeam != null) {
                return oppositeTeam;
            }
        }
        return null;
    }
    
    /**
     * Get the match that the team is in.
     * @param teamName The team to find the match for. 
     * @return The match that the team is in. Null if the given team is not in a match.
     */
    private @Nullable CaptureTheFlagMatch getMatch(@NotNull String teamName) {
        for (CaptureTheFlagMatch match : matches) {
            if (match.getMatchPairing().containsTeam(teamName)) {
                return match;
            }
        }
        return null;
    }
    
    /**
     * @return a copy of this round's matches.
     */
    public @NotNull List<CaptureTheFlagMatch> getMatches() {
        return new ArrayList<>(matches);
    }
    
    // Test methods
    
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (CaptureTheFlagMatch match : matches) {
            sb.append(match);
            sb.append(",");
        }
        return sb.toString();
    }
    
    /**
     * @return a copy of the list of participants
     */
    public @NotNull List<Player> getParticipants() {
        return new ArrayList<>(participants);
    }
    
    /**
     * @return a copy of the list of on-deck participants
     */
    public @NotNull List<Player> getOnDeckParticipants() {
        return new ArrayList<>(onDeckParticipants);
    }
    
    /**
     * @param teamName The team name to check for
     * @return true if the teamName is in one of this round's matches, false otherwise
     */
    public boolean containsTeam(String teamName) {
        for (CaptureTheFlagMatch match : matches) {
            if (match.getMatchPairing().containsTeam(teamName)) {
                return true;
            }
        }
        return false;
    }
}
