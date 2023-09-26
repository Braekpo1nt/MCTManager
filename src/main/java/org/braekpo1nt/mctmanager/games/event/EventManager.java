package org.braekpo1nt.mctmanager.games.event;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.braekpo1nt.mctmanager.games.colossalcolosseum.ColossalColosseumGame;
import org.braekpo1nt.mctmanager.games.event.config.EventStorageUtil;
import org.braekpo1nt.mctmanager.games.game.enums.GameType;
import org.braekpo1nt.mctmanager.games.utils.GameManagerUtils;
import org.braekpo1nt.mctmanager.games.voting.VoteManager;
import org.braekpo1nt.mctmanager.ui.FastBoardManager;
import org.braekpo1nt.mctmanager.ui.TimeStringUtils;
import org.braekpo1nt.mctmanager.ui.sidebar.SidebarManager;
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
    private final EventStorageUtil storageUtil;
    private EventState currentState;
    private EventState lastStateBeforePause;
    private int maxGames = 6;
    private int currentGameNumber = 0;
    private final List<GameType> playedGames = new ArrayList<>();
    /**
     * contains the ScoreKeepers for the games played during the event. Cleared on start and end of event. 
     * <p>
     * If a given key doesn't exist, no score was kept for that game. 
     * <p>
     * If a given key does exist, it is pared with a list of ScoreKeepers which contain the scores
     * tracked for a given iteration of the game. Iterations are in order of play, first to last.
     * If a given iteration is null, then no points were tracked for that iteration. 
     * Otherwise, it contains the scores tracked for the given iteration. 
     */
    private final Map<GameType, List<ScoreKeeper>> scoreKeepers = new HashMap<>();
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
        this.storageUtil = new EventStorageUtil(plugin.getDataFolder());
        this.colossalColosseumGame = new ColossalColosseumGame(plugin, gameManager);
    }
        
    /**
     * The nth multiplier is used on the nth game in the event. If there are x multipliers, and we're on game z where z is greater than x, the xth multiplier is used.
     * @return a multiplier for the score based on the progression in the match.
     */
    public double matchProgressPointMultiplier() {
        if (currentGameNumber <= 0) {
            return 1;
        }
        double[] multipliers = storageUtil.getMultipliers();
        if (currentGameNumber > multipliers.length) {
            return multipliers[multipliers.length - 1];
        }
        return multipliers[currentGameNumber - 1];
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
        
        try {
            storageUtil.loadConfig();
        } catch (IllegalArgumentException e) {
            Bukkit.getLogger().severe(e.getMessage());
            e.printStackTrace();
            sender.sendMessage(Component.text("Can't start event. Error loading config file. See console for details:\n")
                    .append(Component.text(e.getMessage()))
                    .color(NamedTextColor.RED));
            return;
        }
        
        maxGames = numberOfGames;
        currentGameNumber = 1;
        playedGames.clear();
        scoreKeepers.clear();
        initializeSidebar();
        gameManager.getSidebarManager().updateTitle(storageUtil.getTitle());
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
            sender.sendMessage(Component.text("Can't stop the event mid-game.")
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
        clearSidebar();
        currentState = null;
        gameManager.getSidebarManager().updateTitle(SidebarManager.DEFAULT_TITLE);
        cancelAllTasks();
        scoreKeepers.clear();
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
    
    /**
     * 
     * @param sender the sender
     * @param gameType the GameType to undo
     * @param iterationIndex the index of the iteration of the game to undo (0 or more). if gameType has been played n times, and you want to undo the ith play-through, [1, i]&[i, n], pass in iteration=i-1
     */
    public void undoGame(@NotNull CommandSender sender, @NotNull GameType gameType, int iterationIndex) {
        if (currentState == null) {
            sender.sendMessage(Component.text("There isn't an event going on.")
                    .color(NamedTextColor.RED));
            return;
        }
        if (gameManager.getActiveGame() != null && gameManager.getActiveGame().getType().equals(gameType)) {
            sender.sendMessage(Component.text("Can't undo ")
                    .append(Component.text(gameType.getTitle())
                            .decorate(TextDecoration.BOLD))
                    .append(Component.text(" because it is in progress"))
                    .color(NamedTextColor.RED));
            return;
        }
        if (!scoreKeepers.containsKey(gameType)) {
            sender.sendMessage(Component.empty()
                    .append(Component.text("No points were tracked for "))
                    .append(Component.text(gameType.getTitle())
                            .decorate(TextDecoration.BOLD))
                    .append(Component.text("."))
                    .color(NamedTextColor.YELLOW));
            return;
        }
        List<ScoreKeeper> gameScoreKeepers = scoreKeepers.get(gameType);
        if (iterationIndex < 0) {
            sender.sendMessage(Component.empty()
                    .append(Component.text(iterationIndex+1)
                            .decorate(TextDecoration.BOLD))
                    .append(Component.text(" is not a valid play-through"))
                    .color(NamedTextColor.RED));
            return;
        }
        if (iterationIndex >= gameScoreKeepers.size()) {
            sender.sendMessage(Component.text(gameType.getTitle())
                    .append(Component.text(" has only been played "))
                    .append(Component.text(gameScoreKeepers.size()))
                    .append(Component.text(" time(s). Can't undo play-through "))
                    .append(Component.text(iterationIndex + 1))
                    .color(NamedTextColor.RED));
            return;
        }
        ScoreKeeper iterationScoreKeeper = gameScoreKeepers.get(iterationIndex);
        if (iterationScoreKeeper == null) {
            sender.sendMessage(Component.empty()
                    .append(Component.text("No points were tracked for play-through "))
                    .append(Component.text(iterationIndex + 1)
                            .decorate(TextDecoration.BOLD))
                    .append(Component.text(" of "))
                    .append(Component.text(gameType.getTitle())
                            .decorate(TextDecoration.BOLD))
                    .append(Component.text("."))
                    .color(NamedTextColor.YELLOW));
            return;
        }
        undoScores(iterationScoreKeeper);
        gameScoreKeepers.set(iterationIndex, null); // remove tracked points for this iteration
        Component report = createScoreKeeperReport(gameType, iterationScoreKeeper);
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
    private Component createScoreKeeperReport(@NotNull GameType gameType, @NotNull ScoreKeeper scoreKeeper) {
        Set<String> teamNames = gameManager.getTeamNames();
        TextComponent.Builder reportBuilder = Component.text()
                .append(Component.text("|Scores for ("))
                .append(Component.text(gameType.getTitle())
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
    
    public void addGameToVotingPool(@NotNull CommandSender sender, @NotNull GameType gameToAdd) {
        if (currentState == null) {
            sender.sendMessage(Component.text("There isn't an event going on.")
                    .color(NamedTextColor.RED));
            return;
        }
        if (!playedGames.contains(gameToAdd)) {
            sender.sendMessage(Component.text("This game is already in the voting pool.")
                    .color(NamedTextColor.YELLOW));
            return;
        }
        playedGames.remove(gameToAdd);
        sender.sendMessage(Component.text(gameToAdd.getTitle())
                .append(Component.text(" has been added to the voting pool.")));
    }
    
    public void removeGameFromVotingPool(@NotNull CommandSender sender, @NotNull GameType gameToRemove) {
        if (currentState == null) {
            sender.sendMessage(Component.text("There isn't an event going on.")
                    .color(NamedTextColor.RED));
            return;
        }
        playedGames.add(gameToRemove);
        sender.sendMessage(Component.text(gameToRemove.getTitle())
                .append(Component.text(" has been removed from the voting pool")));
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
        double scoreMultiplier = this.matchProgressPointMultiplier();
        gameManager.messageOnlineParticipants(Component.text("Score multiplier: ")
                .append(Component.text(scoreMultiplier))
                .color(NamedTextColor.GOLD));
        this.waitingInHubTaskId = new BukkitRunnable() {
            int count = storageUtil.getWaitingInHubDuration();
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
                if (!allGamesHaveBeenPlayed()) {
                    gameManager.getSidebarManager().updateLine("timer", String.format("Vote starts in: %s", TimeStringUtils.getTimeString(count)));
                } else {
                    gameManager.getSidebarManager().updateLine("timer", String.format("Final round: %s", TimeStringUtils.getTimeString(count)));
                }
                count--;
            }
        }.runTaskTimer(plugin, 0L, 20L).getTaskId();
    }
    
    private void startHalftimeBreak() {
        currentState = EventState.WAITING_IN_HUB;
        gameManager.returnAllParticipantsToHub();
        this.halftimeBreakTaskId = new BukkitRunnable() {
            int count = storageUtil.getHalftimeBreakDuration();
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
                gameManager.getSidebarManager().updateLine("timer", String.format(ChatColor.YELLOW+"Break: %s", TimeStringUtils.getTimeString(count)));
                count--;
            }
        }.runTaskTimer(plugin, 0L, 20L).getTaskId();
    }
    
    private void toPodiumDelay(String winningTeam) {
        currentState = EventState.DELAY;
        ChatColor winningChatColor = gameManager.getTeamChatColor(winningTeam);
        String winningDisplayName = gameManager.getTeamDisplayName(winningTeam);
        initializeSidebar();
        this.toPodiumDelayTaskId = new BukkitRunnable() {
            int count = storageUtil.getBackToHubDuration();
            @Override
            public void run() {
                if (currentState == EventState.PAUSED) {
                    return;
                }
                if (count <= 0) {
                    currentState = EventState.PODIUM;
                    gameManager.getSidebarManager().addLine("winner", String.format("%sWinner: %s", winningChatColor, winningDisplayName));
                    gameManager.getSidebarManager().updateLine("timer", "");
                    gameManager.returnAllParticipantsToPodium(winningTeam);
                    this.cancel();
                    return;
                }
                gameManager.getSidebarManager().updateLine("timer", String.format("Heading to Podium: %s", TimeStringUtils.getTimeString(count)));
                count--;
            }
        }.runTaskTimer(plugin, 0L, 20L).getTaskId();
    }
    
    private void startVoting() {
        currentState = EventState.VOTING;
        List<GameType> votingPool = new ArrayList<>(List.of(GameType.values()));
        votingPool.removeAll(playedGames);
        clearSidebar();
        voteManager.startVote(gameManager.getOnlineParticipants(), votingPool, storageUtil.getVotingDuration(), this::startingGameDelay);
    }
    
    private void startingGameDelay(GameType gameType) {
        currentState = EventState.DELAY;
        initializeSidebar();
        this.startingGameCountdownTaskId = new BukkitRunnable() {
            int count = storageUtil.getStartingGameDuration();
            @Override
            public void run() {
                if (currentState == EventState.PAUSED) {
                    return;
                }
                if (count <= 0) {
                    currentState = EventState.PLAYING_GAME;
                    createScoreKeeperForGame(gameType);
                    clearSidebar();
                    gameManager.startGame(gameType, Bukkit.getConsoleSender());
                    this.cancel();
                    return;
                }
                gameManager.getSidebarManager().updateLine("timer", String.format("%s: %s", gameType.getTitle(),
                        TimeStringUtils.getTimeString(count)));
                count--;
            }
        }.runTaskTimer(plugin, 0L, 20L).getTaskId();
    }
    
    /**
     * Adds a ScoreKeeper to track the game's points. If no ScoreKeepers exist for gameType, creates a new list of iterations for the game.
     * @param gameType the game to add a ScoreKeeper for
     */
    private void createScoreKeeperForGame(GameType gameType) {
        if (!scoreKeepers.containsKey(gameType)) {
            List<ScoreKeeper> iterationScoreKeepers = new ArrayList<>(List.of(new ScoreKeeper()));
            scoreKeepers.put(gameType, iterationScoreKeepers);
        } else {
            List<ScoreKeeper> iterationScoreKeepers = scoreKeepers.get(gameType);
            iterationScoreKeepers.add(new ScoreKeeper());
        }
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
        initializeSidebar();
        this.backToHubDelayTaskId = new BukkitRunnable() {
            int count = storageUtil.getBackToHubDuration();
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
                gameManager.getSidebarManager().updateLine("timer", String.format("Back to Hub: %s", TimeStringUtils.getTimeString(count)));
                count--;
            }
        }.runTaskTimer(plugin, 0L, 20L).getTaskId();
    }
    
    private void toColossalColosseumDelay() {
        currentState = EventState.DELAY;
        this.toColossalColosseumDelayTaskId = new BukkitRunnable() {
            int count = storageUtil.getStartingGameDuration();
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
                gameManager.getSidebarManager().updateLine("timer", String.format("Colossal Colosseum: %s", TimeStringUtils.getTimeString(count)));
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
        clearSidebar();
        startColossalColosseum(Bukkit.getConsoleSender(), firstPlace, secondPlace);
    }
    
    public void startColossalColosseum(CommandSender sender, String firstPlaceTeamName, String secondPlaceTeamName) {
        try {
            if (!colossalColosseumGame.loadConfig()) {
                throw new IllegalArgumentException("Config could not be loaded.");
            }
        } catch (IllegalArgumentException e) {
            Bukkit.getLogger().severe(e.getMessage());
            e.printStackTrace();
            messageAllAdmins(Component.text("Can't start ")
                    .append(Component.text("Colossal Colosseum")
                            .decorate(TextDecoration.BOLD))
                    .append(Component.text(". Error loading config file. See console for details:\n"))
                    .append(Component.text(e.getMessage()))
                    .color(NamedTextColor.RED));
            return;
        }
        gameManager.removeOnlineParticipantsFromHub();
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
            if (currentState != null) {
                startWaitingInHub();
            }
            return;
        }
        if (storageUtil == null) {
            gameManager.gameIsOver();
            return;
        }
        NamedTextColor teamColor = gameManager.getTeamNamedTextColor(winningTeam);
        Bukkit.getServer().sendMessage(Component.empty()
                .append(gameManager.getFormattedTeamDisplayName(winningTeam))
                .append(Component.text(" wins ")
                    .append(Component.text(storageUtil.getTitle()))
                    .append(Component.text("!")))
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
        List<ScoreKeeper> iterationScoreKeepers = scoreKeepers.get(gameType);
        ScoreKeeper iteration = iterationScoreKeepers.get(iterationScoreKeepers.size() - 1);
        iteration.addPoints(teamName, points);
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
        List<ScoreKeeper> iterationScoreKeepers = scoreKeepers.get(gameType);
        ScoreKeeper iteration = iterationScoreKeepers.get(iterationScoreKeepers.size() - 1);
        iteration.addPoints(participantUUID, points);
    }
    
    private void initializeSidebar() {
        gameManager.getSidebarManager().addLine("timer", "");
    }
    
    private void clearSidebar() {
        gameManager.getSidebarManager().deleteLine("timer");
        if (currentState == EventState.PODIUM) {
            gameManager.getSidebarManager().deleteLines("winner");
        }
    }
    
    public boolean eventIsActive() {
        return currentState != null;
    }
    
    /**
     * Check if half the games have been played
     * @return true if the currentGameNumber-1 is half of the maxGames. False if it is lower or higher. 
     * If maxGames is odd, it must be the greater half (i.e. 2 is half of 3, 1 is not). 
     */
    public boolean isItHalfTime() {
        if (maxGames == 1) {
            return false;
        }
        double half = maxGames / 2.0;
        return half <= currentGameNumber-1 && currentGameNumber-1 <= Math.ceil(half);
    }
    
    public boolean allGamesHaveBeenPlayed() {
        return currentGameNumber >= maxGames + 1;
    }
    
    private void messageAllAdmins(Component message) {
        gameManager.messageAdmins(message);
    }
    
    public int getMaxGames() {
        return maxGames;
    }
}
