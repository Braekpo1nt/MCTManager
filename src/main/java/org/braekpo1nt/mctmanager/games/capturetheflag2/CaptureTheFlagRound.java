package org.braekpo1nt.mctmanager.games.capturetheflag2;

import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.braekpo1nt.mctmanager.games.capturetheflag.MatchPairing;
import org.braekpo1nt.mctmanager.games.utils.ParticipantInitializer;
import org.braekpo1nt.mctmanager.ui.FastBoardManager;
import org.braekpo1nt.mctmanager.ui.TimeStringUtils;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

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
    private List<Player> participants;
    private final Location spawnObservatory;
    private int matchesStartingCountDownTaskId;

    public CaptureTheFlagRound(CaptureTheFlagGame captureTheFlagGame, Main plugin, GameManager gameManager, Location spawnObservatory) {
        this.captureTheFlagGame = captureTheFlagGame;
        this.plugin = plugin;
        this.gameManager = gameManager;
        this.spawnObservatory = spawnObservatory;
    }
    
    public void setMatches(List<CaptureTheFlagMatch> matches) {
        this.matches = matches;
    }
    
    public void start(List<Player> newParticipants) {
        participants = new ArrayList<>();
        for (Player participant : newParticipants) {
            initializeParticipant(participant);
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
    
    private void roundIsOver() {
        stop();
        captureTheFlagGame.roundIsOver();
    }
    public void stop() {
        cancelAllTasks();
        for (CaptureTheFlagMatch match : matches) {
            match.stop();
        }
    }
    
    /**
     * Tells the round that the given match is over. If all matches are over, stops the round.
     * @param match The match that is over. Must be one of the matches in {@link CaptureTheFlagRound#matches}.
     */
    public void matchIsOver(CaptureTheFlagMatch match) {
        matches.remove(match);
        if (matches.isEmpty()) {
            roundIsOver();
        }
    }
    
    private void startMatchesStartingCountDown() {
        this.matchesStartingCountDownTaskId = new BukkitRunnable() {
            int count = 15;
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
            List<Player> northParticipants = gameManager.getOnlinePlayersOnTeam(matchPairing.northTeam());
            List<Player> southParticipants = new ArrayList<>();
            for (Player participant : participants) {
                String team = gameManager.getTeamName(participant.getUniqueId());
                if (team.equals(matchPairing.northTeam())) {
                    northParticipants.add(participant);
                } else if (team.equals(matchPairing.southTeam())) {
                    southParticipants.add(participant);
                }
            }
            match.start(northParticipants, southParticipants);
        }
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
            UUID participantUniqueId = participant.getUniqueId();
            gameManager.getFastBoardManager().updateLine(
                    participantUniqueId,
                    5,
                    timeString
            );
        }
    }

    private void initializeFastBoard(Player participant) {
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

    public List<CaptureTheFlagMatch> getMatches() {
        return matches;
    }
    
    
}
