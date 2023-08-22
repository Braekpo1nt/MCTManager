package org.braekpo1nt.mctmanager.games.event;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
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
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class EventManager {
    
    private final Main plugin;
    private final GameManager gameManager;
    private final VoteManager voteManager;
    private final ColossalColosseumGame colossalColosseumGame;
    private EventState currentState;
    private EventState lastStateBeforePause;
    private int maxGames = 6;
    private int currentGameNumber = 1;
    private final List<GameType> playedGames = new ArrayList<>();
    private final Map<GameType, ScoreKeeper> scoreKeepers = new HashMap<>();
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
    private int toPodiumDelayTaskId;
    
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
        playedGames.clear();
        scoreKeepers.clear();
        messageAllAdmins(Component.text("Starting event. On game ")
                .append(Component.text(currentGameNumber))
                .append(Component.text("/"))
                .append(Component.text(maxGames))
                .append(Component.text(".")));
        startWaitingInHub();
    }
    
    public void stopEvent(CommandSender sender) {
        if (currentState == null) {
            sender.sendMessage(Component.text("There is no event running.")
                    .color(NamedTextColor.RED));
            return;
        }
        if (currentState == EventState.PLAYING_GAME) {
            sender.sendMessage(Component.text("Can't stop an event while a game is running.")
                    .color(NamedTextColor.RED));
            return;
        }
        Component message = Component.text("Ending event. ")
                .append(Component.text(currentGameNumber - 1))
                .append(Component.text("/"))
                .append(Component.text(maxGames))
                .append(Component.text(" games were played."));
        sender.sendMessage(message);
        messageAllAdmins(message);
        Bukkit.getLogger().info(String.format("Ending event. %d/%d games were played", currentGameNumber - 1, maxGames));
        if (colossalColosseumGame.isActive()) {
            colossalColosseumGame.stop(null);
        }
        cancelAllTasks();
        currentState = null;
        currentGameNumber = 0;
        maxGames = 6;
    }
    
    public void pauseEvent(CommandSender sender) {
        if (currentState == null) {
            sender.sendMessage(Component.text("There is no event running.")
                    .color(NamedTextColor.RED));
            return;
        }
        if (currentState == EventState.PAUSED) {
            sender.sendMessage(Component.text("The event is already paused.")
                    .color(NamedTextColor.RED));
            return;
        }
        if (currentState == EventState.PLAYING_GAME) {
            sender.sendMessage(Component.text("Can't pause the event during a game.")
                    .color(NamedTextColor.RED));
            return;
        }
        lastStateBeforePause = currentState;
        currentState = EventState.PAUSED;
        voteManager.pauseVote();
        Component pauseMessage = Component.text("The event was paused.")
                .color(NamedTextColor.YELLOW)
                .decorate(TextDecoration.BOLD);
        sender.sendMessage(pauseMessage);
        messageAllAdmins(pauseMessage);
        gameManager.messageOnlineParticipants(pauseMessage);
    }
    
    public void resumeEvent(CommandSender sender) {
        if (currentState == null) {
            sender.sendMessage(Component.text("There isn't an event going on.")
                    .color(NamedTextColor.RED));
            return;
        }
        if (currentState != EventState.PAUSED) {
            sender.sendMessage(Component.text("The event is not paused.")
                    .color(NamedTextColor.RED));
            return;
        }
        currentState = lastStateBeforePause;
        voteManager.resumeVote();
        Component pauseMessage = Component.text("The event was resumed.")
                .color(NamedTextColor.YELLOW)
                .decorate(TextDecoration.BOLD);
        sender.sendMessage(pauseMessage);
        messageAllAdmins(pauseMessage);
        gameManager.messageOnlineParticipants(pauseMessage);
    }
    
    public void undoGame(@NotNull CommandSender sender, @NotNull GameType gameType) {
        if (currentState == null) {
            sender.sendMessage(Component.text("There isn't an event going on.")
                    .color(NamedTextColor.RED));
            return;
        }
        if (gameManager.getActiveGame() != null && gameManager.getActiveGame().getType().equals(gameType)) {
            sender.sendMessage(Component.text("Can't undo ")
                    .append(Component.text(GameType.getTitle(gameType))
                            .decorate(TextDecoration.BOLD))
                    .append(Component.text(" because it is in progress"))
                    .color(NamedTextColor.RED));
            return;
        }
        if (!playedGames.contains(gameType)) {
            sender.sendMessage(Component.empty()
                    .append(Component.text("This game has not been played yet."))
                    .color(NamedTextColor.RED));
            return;
        }
        if (!scoreKeepers.containsKey(gameType)) {
            sender.sendMessage(Component.empty()
                    .append(Component.text("No points were tracked for "))
                    .append(Component.text(GameType.getTitle(gameType))
                            .decorate(TextDecoration.BOLD))
                    .append(Component.text("."))
                    .color(NamedTextColor.YELLOW));
            return;
        }
        
        ScoreKeeper scoreKeeper = scoreKeepers.remove(gameType);
        undoScores(scoreKeeper);
        Component report = createUndoReport(gameType, scoreKeeper);
        sender.sendMessage(report);
        Bukkit.getConsoleSender().sendMessage(report);
    }
    
    /**
     * Removes the scores that were tracked by the given ScoreKeeper
     * @param scoreKeeper holds the tracked scores to be removed
     */
    private void undoScores(ScoreKeeper scoreKeeper) {
        Set<String> teamNames = gameManager.getTeamNames();
        for (String teamName : teamNames) {
            int teamScoreToSubtract = scoreKeeper.getScore(teamName);
            int teamCurrentScore = gameManager.getScore(teamName);
            if (teamCurrentScore - teamScoreToSubtract < 0) {
                teamScoreToSubtract = teamCurrentScore;
            }
            gameManager.addScore(teamName, -teamScoreToSubtract);
        
            List<UUID> participantUUIDs = gameManager.getParticipantUUIDsOnTeam(teamName);
            for (UUID participantUUID : participantUUIDs) {
                int participantScoreToSubtract = scoreKeeper.getScore(participantUUID);
                int participantCurrentScore = gameManager.getScore(participantUUID);
                if (participantCurrentScore - participantScoreToSubtract < 0) {
                    participantScoreToSubtract = participantCurrentScore;
                }
                gameManager.addScore(participantUUID, -participantScoreToSubtract);
            }
        }
    }
    
    /**
     * Creates a report describing the scores associated with the given ScoreKeeper
     * @param gameType The gameType the ScoreKeeper is associated with
     * @param scoreKeeper The scorekeeper describing the given scores
     * @return A component with a report of the ScoreKeeper's scores
     */
    @NotNull
    private Component createUndoReport(@NotNull GameType gameType, @NotNull ScoreKeeper scoreKeeper) {
        Set<String> teamNames = gameManager.getTeamNames();
        TextComponent.Builder reportBuilder = Component.text()
                .append(Component.text("|Scores removed ("))
                .append(Component.text(GameType.getTitle(gameType))
                        .decorate(TextDecoration.BOLD))
                .append(Component.text("):\n"))
                .color(NamedTextColor.YELLOW);
        for (String teamName : teamNames) {
            int teamScoreToSubtract = scoreKeeper.getScore(teamName);
            NamedTextColor teamColor = gameManager.getTeamNamedTextColor(teamName);
            Component displayName = gameManager.getFormattedTeamDisplayName(teamName);
            reportBuilder.append(Component.text("|  - "))
                    .append(displayName)
                    .append(Component.text(": "))
                    .append(Component.text(teamScoreToSubtract)
                            .color(NamedTextColor.GOLD)
                            .decorate(TextDecoration.BOLD))
                    .append(Component.text("\n"));
        
            List<UUID> participantUUIDs = gameManager.getParticipantUUIDsOnTeam(teamName);
            for (UUID participantUUID : participantUUIDs) {
                Player participant = Bukkit.getPlayer(participantUUID);
                if (participant != null) {
                    int participantScoreToSubtract = scoreKeeper.getScore(participantUUID);
                    reportBuilder.append(Component.text("|    - "))
                            .append(Component.text(participant.getName())
                                    .color(teamColor))
                            .append(Component.text(": "))
                            .append(Component.text(participantScoreToSubtract)
                                    .color(NamedTextColor.GOLD)
                                    .decorate(TextDecoration.BOLD))
                            .append(Component.text("\n"));
                }
            }
        }
        return reportBuilder.build();
    }
    
    public void cancelAllTasks() {
        Bukkit.getScheduler().cancelTask(waitingInHubTaskId);
        Bukkit.getScheduler().cancelTask(toColossalColosseumDelayTaskId);
        Bukkit.getScheduler().cancelTask(backToHubDelayTaskId);
        Bukkit.getScheduler().cancelTask(startingGameCountdownTaskId);
        Bukkit.getScheduler().cancelTask(halftimeBreakTaskId);
        Bukkit.getScheduler().cancelTask(toPodiumDelayTaskId);
        voteManager.cancelVote();
    }
    
    private void startWaitingInHub() {
        currentState = EventState.WAITING_IN_HUB;
        gameManager.returnAllParticipantsToHub();
        this.waitingInHubTaskId = new BukkitRunnable() {
            int count = WAITING_IN_HUB_DURATION;
            @Override
            public void run() {
                if (currentState == EventState.PAUSED) {
                    return;
                }
                if (count <= 0) {
                    if (allGamesHaveBeenPlayed()) {
                        toColossalColosseumDelay();
                    } else {
                        startVoting();
                    }
                    this.cancel();
                    return;
                }
                updateTimerFastBoard(String.format("Vote starts in: %s", TimeStringUtils.getTimeString(count)));
                count--;
            }
        }.runTaskTimer(plugin, 0L, 20L).getTaskId();
    }
    
    private void startHalftimeBreak() {
        currentState = EventState.WAITING_IN_HUB;
        gameManager.returnAllParticipantsToHub();
        this.halftimeBreakTaskId = new BukkitRunnable() {
            int count = HALFTIME_BREAK_DURATION;
            @Override
            public void run() {
                if (currentState == EventState.PAUSED) {
                    return;
                }
                if (count <= 0) {
                    startVoting();
                    this.cancel();
                    return;
                }
                updateTimerFastBoard(String.format(ChatColor.YELLOW+"Break: %s", TimeStringUtils.getTimeString(count)));
                count--;
            }
        }.runTaskTimer(plugin, 0L, 20L).getTaskId();
    }
    
    private void toPodiumDelay(String winningTeam) {
        currentState = EventState.DELAY;
        this.toPodiumDelayTaskId = new BukkitRunnable() {
            int count = BACK_TO_HUB_DELAY_DURATION;
            @Override
            public void run() {
                if (currentState == EventState.PAUSED) {
                    return;
                }
                if (count <= 0) {
                    gameManager.returnAllParticipantsToPodium(winningTeam);
                    this.cancel();
                    return;
                }
                updateTimerFastBoard(String.format("Heading to Podium: %s", TimeStringUtils.getTimeString(count)));
                count--;
            }
        }.runTaskTimer(plugin, 0L, 20L).getTaskId();
    }
    
    private void startVoting() {
        currentState = EventState.VOTING;
        List<GameType> votingPool = new ArrayList<>(List.of(GameType.values()));
        votingPool.removeAll(playedGames);
        voteManager.startVote(gameManager.getOnlineParticipants(), votingPool, VOTING_DURATION, this::startingGameDelay);
    }
    
    private void startingGameDelay(GameType gameType) {
        currentState = EventState.DELAY;
        this.startingGameCountdownTaskId = new BukkitRunnable() {
            int count = STARTING_GAME_COUNT_DOWN_DURATION;
            @Override
            public void run() {
                if (currentState == EventState.PAUSED) {
                    return;
                }
                if (count <= 0) {
                    currentState = EventState.PLAYING_GAME;
                    gameManager.startGame(gameType, Bukkit.getConsoleSender());
                    this.cancel();
                    return;
                }
                updateTimerFastBoard(String.format("%s: %s", GameType.getTitle(gameType),
                        TimeStringUtils.getTimeString(count)));
                count--;
            }
        }.runTaskTimer(plugin, 0L, 20L).getTaskId();
    }
    
    /**
     * To be called when a game is over. The GameManager knows when a game ends, 
     * and thus can call this method if an event is running.
     * @param finishedGameType The GameType of the game that just ended
     */
    public void gameIsOver(GameType finishedGameType) {
        currentState = EventState.DELAY;
        playedGames.add(finishedGameType);
        currentGameNumber += 1;
        this.backToHubDelayTaskId = new BukkitRunnable() {
            int count = BACK_TO_HUB_DELAY_DURATION;
            @Override
            public void run() {
                if (currentState == EventState.PAUSED) {
                    return;
                }
                if (count <= 0) {
                    if (isItHalfTime()) {
                        startHalftimeBreak();
                    } else {
                        startWaitingInHub();
                    }
                    this.cancel();
                    return;
                }
                updateTimerFastBoard(String.format("Back to Hub: %s", TimeStringUtils.getTimeString(count)));
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
                if (currentState == EventState.PAUSED) {
                    return;
                }
                if (count <= 0) {
                    // start selected game
                    currentState = EventState.PLAYING_GAME;
                    identifyWinnersAndStartColossalColosseum();
                    this.cancel();
                    return;
                }
                updateTimerFastBoard(String.format("Colossal Colosseum: %s", TimeStringUtils.getTimeString(count)));
                count--;
            }
        }.runTaskTimer(plugin, 0L, 20L).getTaskId();
    }
    
    public void identifyWinnersAndStartColossalColosseum() {
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
        if (colossalColosseumGame.isActive()) {
            sender.sendMessage(Component.text("Colossal Colosseum is already running").color(NamedTextColor.RED));
            return;
        }
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
    
    public void stopColossalColosseum(CommandSender sender) {
        if (!colossalColosseumGame.isActive()) {
            sender.sendMessage(Component.text("Colossal Colosseum is not running")
                    .color(NamedTextColor.RED));
            return;
        }
        colossalColosseumGame.stop(null);
    }
    
    /**
     * Called when Colossal Colosseum is over. If the passed in winningTeam name is null,
     * nothing happens. Otherwise, this initiates the podium process for the winning team.
     * @param winningTeam The name of the winning team. If this is null, nothing happens.
     */
    public void colossalColosseumIsOver(@Nullable String winningTeam) {
        if (winningTeam == null) {
            return;
        }
        NamedTextColor teamColor = gameManager.getTeamNamedTextColor(winningTeam);
        Bukkit.getServer().sendMessage(Component.empty()
                .append(gameManager.getFormattedTeamDisplayName(winningTeam))
                .append(Component.text(" wins MCT #5!"))
                .color(teamColor)
                .decorate(TextDecoration.BOLD));
        toPodiumDelay(winningTeam);
    }
    
    /**
     * Track the points earned for the given team in the given game. 
     * If the event is not active, nothing happens.
     * @param teamName The team to track points for
     * @param points the points to add
     * @param gameType the game that the points came from
     */
    public void trackPoints(String teamName, int points, GameType gameType) {
        if (currentState == null) {
            return;
        }
        if (!scoreKeepers.containsKey(gameType)) {
            scoreKeepers.put(gameType, new ScoreKeeper());
        }
        ScoreKeeper scoreKeeper = scoreKeepers.get(gameType);
        scoreKeeper.addPoints(teamName, points);
    }
    
    /**
     * Track the points earned for the given participant in the given game. 
     * If the event is not active, nothing happens.
     * @param participantUUID The participant to track points for
     * @param points the points to add 
     * @param gameType the game that the points came from
     */
    public void trackPoints(UUID participantUUID, int points, GameType gameType) {
        if (currentState == null) {
            return;
        }
        if (!scoreKeepers.containsKey(gameType)) {
            scoreKeepers.put(gameType, new ScoreKeeper());
        }
        ScoreKeeper scoreKeeper = scoreKeepers.get(gameType);
        scoreKeeper.addPoints(participantUUID, points);
    }
    
    // FastBoard start
    
    private void updateTimerFastBoard(String time) {
        for (Player participant : gameManager.getOnlineParticipants()) {
            updateTimerFastBoard(participant, time);
        }
    }
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
        return currentGameNumber == maxGames + 1;
    }
    
    private void messageAllAdmins(Component message) {
        gameManager.messageAdmins(message);
    }
}
