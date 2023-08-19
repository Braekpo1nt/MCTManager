package org.braekpo1nt.mctmanager.games.event;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.braekpo1nt.mctmanager.games.colossalcolosseum.ColossalColosseumGame;
import org.braekpo1nt.mctmanager.games.enums.GameType;
import org.braekpo1nt.mctmanager.games.utils.GameManagerUtils;
import org.braekpo1nt.mctmanager.games.voting.VoteManager;
import org.braekpo1nt.mctmanager.ui.TimeStringUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class EventManager {
    
    private final Main plugin;
    private final GameManager gameManager;
    private final VoteManager voteManager;
    private final ColossalColosseumGame colossalColosseumGame;
    private EventState currentState;
    private int maxGames = 6;
    private int currentGameNumber = 0;
    // Config stuff
    // Durations in seconds
    private final int WAITING_IN_HUB_DURATION = 20;
    private final int HALFTIME_BREAK_DURATION = 60;
    private final int VOTING_DURATION = 20;
    private final int STARTING_GAME_COUNT_DOWN_DURATION = 5;
    private final int BACK_TO_HUB_DELAY_DURATION = 10;
    // Task IDs
    private int waitingInHubTaskId;
    private int toColossalColosseumDelayTaskId;
    private int backToHubDelayTaskId;
    private int startingGameCountdownTaskId;
    private int halftimeBreakTaskId;
    
    public EventManager(Main plugin, GameManager gameManager, VoteManager voteManager) {
        this.plugin = plugin;
        this.gameManager = gameManager;
        this.voteManager = voteManager;
        this.colossalColosseumGame = new ColossalColosseumGame(plugin, gameManager);
    }
    
    /**
     * Start a new event with the given number of games
     * @param sender the place to send errors and confirmations to
     * @param numberOfGames the number of games to be played in this event
     */
    public void startEvent(CommandSender sender, int numberOfGames) {
        if (currentState != null) {
            sender.sendMessage(Component.text("An event is already running.")
                    .color(NamedTextColor.RED));
            return;
        }
        if (gameManager.getActiveGame() != null) {
            sender.sendMessage(Component.text("Can't start an event while a game is running.")
                    .color(NamedTextColor.RED));
            return;
        }
        maxGames = numberOfGames;
        currentGameNumber = 1;
        gameManager.clearPlayedGames();
        messageAllAdmins(Component.text("Starting event. On game ")
                .append(Component.text(currentGameNumber))
                .append(Component.text("/"))
                .append(Component.text(maxGames))
                .append(Component.text(".")));
        startWaitingInHub();
    }
    
    public void stopEvent(CommandSender sender) {}
    
    public void pauseEvent(CommandSender sender) {}
    
    public void resumeEvent(CommandSender sender) {}
    
    public void undoGame(@NotNull CommandSender sender, @NotNull GameType gameType) {}
    
    public void cancelAllTasks() {
        Bukkit.getScheduler().cancelTask(waitingInHubTaskId);
        Bukkit.getScheduler().cancelTask(toColossalColosseumDelayTaskId);
        Bukkit.getScheduler().cancelTask(backToHubDelayTaskId);
        Bukkit.getScheduler().cancelTask(startingGameCountdownTaskId);
        Bukkit.getScheduler().cancelTask(halftimeBreakTaskId);
    }
    
    private void startWaitingInHub() {
        currentState = EventState.WAITING_IN_HUB;
        this.waitingInHubTaskId = new BukkitRunnable() {
            int count = WAITING_IN_HUB_DURATION;
            @Override
            public void run() {
                if (count <= 0) {
                    if (allGamesHaveBeenPlayed()) {
                        toColossalColosseumDelay();
                    } else {
                        startVoting();
                    }
                    this.cancel();
                    return;
                }
                String timeString = "Hub: " + TimeStringUtils.getTimeString(count);
                for (Player participant : gameManager.getOnlineParticipants()) {
                    updateTimerFastBoard(participant, timeString);
                }
                count--;
            }
        }.runTaskTimer(plugin, 0L, 20L).getTaskId();
    }
    
    private void startHalftimeBreak() {
        currentState = EventState.WAITING_IN_HUB;
        this.halftimeBreakTaskId = new BukkitRunnable() {
            int count = HALFTIME_BREAK_DURATION;
            @Override
            public void run() {
                if (count <= 0) {
                    startVoting();
                    this.cancel();
                    return;
                }
                count--;
            }
        }.runTaskTimer(plugin, 0L, 20L).getTaskId();
    }
    
    private void startVoting() {
        currentState = EventState.VOTING;
        List<GameType> votingPool = new ArrayList<>(List.of(GameType.values()));
        votingPool.removeAll(gameManager.getPlayedGames());
        voteManager.startVote(gameManager.getOnlineParticipants(), votingPool, VOTING_DURATION, this::startingGameDelay);
    }
    
    private void startingGameDelay(GameType gameType) {
        currentState = EventState.DELAY;
        this.startingGameCountdownTaskId = new BukkitRunnable() {
            int count = STARTING_GAME_COUNT_DOWN_DURATION;
            @Override
            public void run() {
                if (count <= 0) {
                    currentState = EventState.PLAYING_GAME;
                    gameManager.startGame(gameType, Bukkit.getConsoleSender());
                    this.cancel();
                    return;
                }
                count--;
            }
        }.runTaskTimer(plugin, 0L, 20L).getTaskId();
    }
    
    public void gameIsOver() {
        currentState = EventState.DELAY;
        this.backToHubDelayTaskId = new BukkitRunnable() {
            int count = BACK_TO_HUB_DELAY_DURATION;
            @Override
            public void run() {
                if (count <= 0) {
                    if (isItHalfTime()) {
                        startHalftimeBreak();
                    } else {
                        startWaitingInHub();
                    }
                    this.cancel();
                    return;
                }
                count--;
            }
        }.runTaskTimer(plugin, 0L, 20L).getTaskId();
    }
    
    private void toColossalColosseumDelay() {
        currentState = EventState.DELAY;
        this.toColossalColosseumDelayTaskId = new BukkitRunnable() {
            int count = STARTING_GAME_COUNT_DOWN_DURATION;
            @Override
            public void run() {
                if (count <= 0) {
                    // start selected game
                    currentState = EventState.PLAYING_GAME;
                    startColossalColosseum();
                    this.cancel();
                    return;
                }
                count--;
            }
        }.runTaskTimer(plugin, 0L, 20L).getTaskId();
    }
    
    public void startColossalColosseum() {
        Set<String> allTeams = gameManager.getTeamNames();
        if (allTeams.size() < 2) {
            messageAllAdmins(Component.empty()
                    .append(Component.text("There are fewer than two teams online. Use "))
                    .append(Component.text("/mct game finalgame <first> <second>")
                            .clickEvent(ClickEvent.suggestCommand("/mct game finalgame "))
                            .decorate(TextDecoration.BOLD))
                    .append(Component.text(" to start the final game with the two chosen teams.")));
            return;
        }
        Map<String, Integer> teamScores = new HashMap<>();
        for (String teamName : allTeams) {
            int score = gameManager.getScore(teamName);
            teamScores.put(teamName, score);
        }
        String[] firstPlaces = GameManagerUtils.calculateFirstPlace(teamScores);
        if (firstPlaces.length == 2) {
            String firstPlace = firstPlaces[0];
            String secondPlace = firstPlaces[1];
            startColossalColosseum(Bukkit.getConsoleSender(), firstPlace, secondPlace);
            return;
        }
        if (firstPlaces.length > 2) {
            messageAllAdmins(Component.text("There are more than 2 teams tied for first place. A tie breaker is needed. Use ")
                    .append(Component.text("/mct game finalgame <first> <second>")
                            .clickEvent(ClickEvent.suggestCommand("/mct game finalgame "))
                            .decorate(TextDecoration.BOLD))
                    .append(Component.text(" to start the final game with the two chosen teams."))
                    .color(NamedTextColor.RED));
            return;
        }
        String firstPlace = firstPlaces[0];
        teamScores.remove(firstPlace);
        String[] secondPlaces = GameManagerUtils.calculateFirstPlace(teamScores);
        if (secondPlaces.length > 1) {
            messageAllAdmins(Component.text("There is a tie second place. A tie breaker is needed. Use ")
                    .append(Component.text("/mct game finalgame <first> <second>")
                            .clickEvent(ClickEvent.suggestCommand("/mct game finalgame "))
                            .decorate(TextDecoration.BOLD))
                    .append(Component.text(" to start the final game with the two chosen teams."))
                    .color(NamedTextColor.RED));
            return;
        }
        String secondPlace = secondPlaces[0];
        startColossalColosseum(Bukkit.getConsoleSender(), firstPlace, secondPlace);
    }
    
    public void startColossalColosseum(CommandSender sender, String firstPlaceTeamName, String secondPlaceTeamName) {
        if (firstPlaceTeamName == null || secondPlaceTeamName == null) {
            sender.sendMessage(Component.text("Please specify the first and second place teams.").color(NamedTextColor.RED));
            return;
        }
        
        List<Player> firstPlaceParticipants = new ArrayList<>();
        List<Player> secondPlaceParticipants = new ArrayList<>();
        List<Player> spectators = new ArrayList<>();
        for (Player participant : gameManager.getOnlineParticipants()) {
            String teamName = gameManager.getTeamName(participant.getUniqueId());
            if (teamName.equals(firstPlaceTeamName)) {
                firstPlaceParticipants.add(participant);
            } else if (teamName.equals(secondPlaceTeamName)) {
                secondPlaceParticipants.add(participant);
            } else {
                spectators.add(participant);
            }
        }
        
        if (firstPlaceParticipants.isEmpty()) {
            sender.sendMessage(Component.text("There are no members of the first place team online.").color(NamedTextColor.RED));
            return;
        }
        
        if (secondPlaceParticipants.isEmpty()) {
            sender.sendMessage(Component.text("There are no members of the second place team online.").color(NamedTextColor.RED));
            return;
        }
        
        colossalColosseumGame.start(firstPlaceParticipants, secondPlaceParticipants, spectators);
    }
    
    public void finalGameIsOver(String winningTeamName) {
        Bukkit.getLogger().info("Called \"finalGameIsOver\" method: " + winningTeamName);
        throw new UnsupportedOperationException("EventManager#finalGameIsOver is not implemented yet");
    }
    
    // FastBoard start
    private void updateTimerFastBoard(Player participant, String time) {
        gameManager.getFastBoardManager().updateLine(
                participant.getUniqueId(),
                1,
                time
        );
    }
    
    // FastBoard end
    
    public boolean eventIsActive() {
        return currentState != null;
    }
    
    /**
     * Check if half the games have been played
     * @return true if the currentGameNumber-1 is half of the maxGames. False if it is lower or higher. 
     * If maxGames is odd, it must be the greater half (i.e. 2 is half of 3, 1 is not). 
     */
    public boolean isItHalfTime() {
        double half = maxGames / 2.0;
        return half <= currentGameNumber-1 && currentGameNumber-1 <= Math.ceil(half);
    }
    
    public boolean allGamesHaveBeenPlayed() {
        return currentGameNumber == maxGames - 1;
    }
    
    private void messageAllAdmins(Component message) {
        gameManager.messageAdmins(message);
    }
}
