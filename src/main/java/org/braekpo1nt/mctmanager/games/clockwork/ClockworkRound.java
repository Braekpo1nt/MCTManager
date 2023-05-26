package org.braekpo1nt.mctmanager.games.clockwork;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.braekpo1nt.mctmanager.games.utils.ParticipantInitializer;
import org.braekpo1nt.mctmanager.ui.TimeStringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.awt.*;
import java.util.*;
import java.util.List;

public class ClockworkRound implements Listener {

    private final Main plugin;
    private final GameManager gameManager;
    private final ClockworkGame clockworkGame;
    private final Location startingPosition;
    private Map<UUID, Boolean> participantsAreAlive;
    private Map<String, Boolean> teamsAreAlive;
    private List<Player> participants;
    private static final String title = ChatColor.BLUE+"Clockwork";
    private boolean roundActive;
    private int roundStartingCountDownTaskId;
    private int bellCountDownTaskId;

    public ClockworkRound(Main plugin, GameManager gameManager, ClockworkGame clockworkGame, Location startingPosition) {
        this.plugin = plugin;
        this.gameManager = gameManager;
        this.clockworkGame = clockworkGame;
        this.startingPosition = startingPosition;
    }
    
    public void start(List<Player> newParticipants) {
        participants = new ArrayList<>(newParticipants.size());
        participantsAreAlive = new HashMap<>(newParticipants.size());
        teamsAreAlive = new HashMap<>();
        List<String> teams = gameManager.getTeamNames(newParticipants);
        for (String team : teams) {
            teamsAreAlive.put(team, true);
        }
        String livingTeams = ""+teamsAreAlive.size();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        for (Player participant : newParticipants) {
            initializeParticipant(participant, livingTeams);
        }
        setupTeamOptions();
        startRoundStartingCountDown();
        roundActive = true;
        Bukkit.getLogger().info("Starting capture the flag round");
    }

    private void roundIsOver() {
        stop();
        clockworkGame.roundIsOver();
    }

    public void stop() {
        roundActive = false;
        HandlerList.unregisterAll(this);
        cancelAllTasks();
        for (Player participant : participants) {
            resetParticipant(participant);
        }
        participants.clear();
        participantsAreAlive.clear();
        Bukkit.getLogger().info("Stopping clockwork round");
    }
    
    private void startRoundStartingCountDown() {
        this.roundStartingCountDownTaskId = new BukkitRunnable() {
            int count = 10;
            @Override
            public void run() {
                if (count <= 0) {
                    startClockwork();
                    this.cancel();
                    return;
                }
                String timeLeft = TimeStringUtils.getTimeString(count);
                for (Player participant : participants){
                    updateRoundStartingCountDown(participant, timeLeft);
                }
                count--;
            }
        }.runTaskTimer(plugin, 0L, 20L).getTaskId();
    }

    private void startClockwork() {
        messageAllParticipants(Component.text("Listen to the bell!"));
        ringBell();
    }
    
    private void ringBell() {
        int numberOfBellRings = new Random().nextInt(1, 13);
        messageAllParticipants(Component.text("The bell rings ")
                .append(Component.text(numberOfBellRings))
                .append(Component.text(" times"))
                .color(NamedTextColor.GREEN)
                .decorate(TextDecoration.BOLD));
        startBellCountDown();
    }

    private void startBellCountDown() {
        this.bellCountDownTaskId = new BukkitRunnable() {
            int count = 5;
            @Override
            public void run() {
                if (count <= 0) {
                    onBellCountDownRunOut();
                    this.cancel();
                    return;
                }
                String timeLeft = ""+count;
                for (Player participant : participants) {
                    updateBellCountDownFastBoard(participant, timeLeft);
                }
                count--;
            }
        }.runTaskTimer(plugin, 0L, 20L).getTaskId();
    }

    private void onBellCountDownRunOut() {
        messageAllParticipants(Component.text("All players not on the right wedge are dead"));
        this.bellCountDownTaskId = new BukkitRunnable() {
            int count = 3;
            @Override
            public void run() {
                if (count <= 0) {
                    ringBell();
                    this.cancel();
                    return;
                }
                String timeLeft = ""+count;
                for (Player participant : participants) {
                    updateBellCountDownFastBoard(participant, timeLeft);
                }
                count--;
            }
        }.runTaskTimer(plugin, 0L, 20L).getTaskId();
    }

    private void setupTeamOptions() {
        Scoreboard mctScoreboard = gameManager.getMctScoreboard();
        for (Team team : mctScoreboard.getTeams()) {
            team.setAllowFriendlyFire(false);
            team.setCanSeeFriendlyInvisibles(true);
            team.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.FOR_OTHER_TEAMS);
            team.setOption(Team.Option.DEATH_MESSAGE_VISIBILITY, Team.OptionStatus.ALWAYS);
            team.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.ALWAYS);
        }
    }

    private void resetParticipant(Player participant) {
        participant.getInventory().clear();
        participant.setGameMode(GameMode.ADVENTURE);
        ParticipantInitializer.clearStatusEffects(participant);
        ParticipantInitializer.resetHealthAndHunger(participant);
    }

    private void cancelAllTasks() {
        Bukkit.getScheduler().cancelTask(roundStartingCountDownTaskId);
        Bukkit.getScheduler().cancelTask(bellCountDownTaskId);
    }

    private void updateBellCountDownFastBoard(Player participant, String timeLeft) {
        gameManager.getFastBoardManager().updateLine(
                participant.getUniqueId(),
                6,
                timeLeft
        );
    }

    private void updateRoundStartingCountDown(Player participant, String timeLeft) {
        gameManager.getFastBoardManager().updateLine(
                participant.getUniqueId(),
                3,
                "Starting:"
        );
        gameManager.getFastBoardManager().updateLine(
                participant.getUniqueId(),
                4,
                timeLeft
        );
    }

    private void initializeParticipant(Player participant, String livingTeams) {
        participants.add(participant);
        UUID participantUniqueId = participant.getUniqueId();
        participantsAreAlive.put(participantUniqueId, true);
        initializeFastBoard(participant, livingTeams);
        participant.teleport(startingPosition);
        participant.getInventory().clear();
        participant.setGameMode(GameMode.ADVENTURE);
        ParticipantInitializer.clearStatusEffects(participant);
        ParticipantInitializer.resetHealthAndHunger(participant);
    }

    private void initializeFastBoard(Player participant, String livingTeams) {
        gameManager.getFastBoardManager().updateLine(
                participant.getUniqueId(),
                4,
                livingTeams// teams alive
        );
        gameManager.getFastBoardManager().updateLine(
                participant.getUniqueId(),
                6,
                ""
        );
    }
    
    private void updateTeamsAliveFastBoard(Player participant, String livingTeams) {
        gameManager.getFastBoardManager().updateLine(
                participant.getUniqueId(),
                4,
                ""+livingTeams // teams alive
        );
    }
    
    private void messageAllParticipants(Component message) {
        for (Player participant : participants) {
            participant.sendMessage(message);
        }
    }
}
