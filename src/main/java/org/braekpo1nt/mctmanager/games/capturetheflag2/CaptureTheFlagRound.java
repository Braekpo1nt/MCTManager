package org.braekpo1nt.mctmanager.games.capturetheflag2;

import org.braekpo1nt.mctmanager.Main;
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

    private final Main plugin;
    private final FastBoardManager fastBoardManager;
    private final List<CaptureTheFlagMatch> matches;
    private List<Player> participants;
    private final Location spawnObservatory;
    private int matchesStartingCountDownTaskId;

    public CaptureTheFlagRound(Main plugin, FastBoardManager fastBoardManager, List<CaptureTheFlagMatch> matches, Location spawnObservatory) {
        this.plugin = plugin;
        this.fastBoardManager = fastBoardManager;
        this.matches = matches;
        this.spawnObservatory = spawnObservatory;
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

    public void stop() {
        cancelAllTasks();
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
            match.start();
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
            fastBoardManager.updateLine(
                    participantUniqueId,
                    5,
                    timeString
            );
        }
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

    public List<CaptureTheFlagMatch> getMatches() {
        return matches;
    }
    
    
}
