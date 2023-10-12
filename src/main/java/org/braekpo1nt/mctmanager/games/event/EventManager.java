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
import org.braekpo1nt.mctmanager.ui.TimeStringUtils;
import org.braekpo1nt.mctmanager.ui.sidebar.KeyLine;
import org.braekpo1nt.mctmanager.ui.sidebar.Sidebar;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class EventManager implements Listener {
    
    private final Main plugin;
    private final GameManager gameManager;
    private Sidebar sidebar;
    private Sidebar adminSidebar;
    private int numberOfTeams = 0;
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
    private List<Player> participants = new ArrayList<>();
    private List<Player> admins = new ArrayList<>();
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
        
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        maxGames = numberOfGames;
        currentGameNumber = 1;
        playedGames.clear();
        scoreKeepers.clear();
        sidebar = gameManager.getSidebarFactory().createSidebar();
        participants = new ArrayList<>();
        for (Player participant : gameManager.getOnlineParticipants()) {
            participants.add(participant);
            sidebar.addPlayer(participant);
        }
        initializeSidebar();
        admins = new ArrayList<>();
        adminSidebar = gameManager.getSidebarFactory().createSidebar();
        for (Player admin : gameManager.getOnlineAdmins()) {
            admins.add(admin);
            adminSidebar.addPlayer(admin);
        }
        initializeAdminSidebar();
        messageAllAdmins(Component.text("Starting event. On game ")
                .append(Component.text(currentGameNumber))
                .append(Component.text("/"))
                .append(Component.text(maxGames))
                .append(Component.text(".")));
        gameManager.removeParticipantsFromHub(participants);
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
        HandlerList.unregisterAll(this);
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
        admins.clear();
        clearAdminSidebar();
        currentState = null;
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
    
    public boolean colossalColosseumIsActive() {
        return colossalColosseumGame.isActive();
    }
    
    public void onParticipantJoin(Player participant) {
        if (colossalColosseumGame.isActive()) {
            colossalColosseumGame.onParticipantJoin(participant);
        }
        if (currentState == null) {
            return;
        }
        
    }
    
    public void onParticipantQuit(Player participant) {
        if (colossalColosseumGame.isActive()) {
            colossalColosseumGame.onParticipantQuit(participant);
        }
        if (currentState == null) {
            return;
        }
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
                    sidebar.updateLine("timer", String.format("Vote starts in: %s", TimeStringUtils.getTimeString(count)));
                    adminSidebar.updateLine("timer", String.format("Vote starts in: %s", TimeStringUtils.getTimeString(count)));
                } else {
                    sidebar.updateLine("timer", String.format("Final round: %s", TimeStringUtils.getTimeString(count)));
                    adminSidebar.updateLine("timer", String.format("Final round: %s", TimeStringUtils.getTimeString(count)));
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
                sidebar.updateLine("timer", String.format(ChatColor.YELLOW+"Break: %s", TimeStringUtils.getTimeString(count)));
                adminSidebar.updateLine("timer", String.format(ChatColor.YELLOW+"Break: %s", TimeStringUtils.getTimeString(count)));
                count--;
            }
        }.runTaskTimer(plugin, 0L, 20L).getTaskId();
    }
    
    private void toPodiumDelay(String winningTeam) {
        currentState = EventState.DELAY;
        for (Player participant : gameManager.getOnlineParticipants()) {
            participants.add(participant);
            sidebar.addPlayer(participant);
        }
        for (Player admin : gameManager.getOnlineAdmins()) {
            admins.add(admin);
            adminSidebar.addPlayer(admin);
        }
        updateTeamScores();
        updatePersonalScores();
        ChatColor winningChatColor = gameManager.getTeamChatColor(winningTeam);
        String winningDisplayName = gameManager.getTeamDisplayName(winningTeam);
        this.toPodiumDelayTaskId = new BukkitRunnable() {
            int count = storageUtil.getBackToHubDuration();
            @Override
            public void run() {
                if (currentState == EventState.PAUSED) {
                    return;
                }
                if (count <= 0) {
                    currentState = EventState.PODIUM;
                    sidebar.addLine("winner", String.format("%sWinner: %s", winningChatColor, winningDisplayName));
                    sidebar.updateLine("timer", "");
                    adminSidebar.addLine("winner", String.format("%sWinner: %s", winningChatColor, winningDisplayName));
                    adminSidebar.updateLine("timer", "");
                    gameManager.returnAllParticipantsToPodium(winningTeam);
                    this.cancel();
                    return;
                }
                sidebar.updateLine("timer", String.format("Heading to Podium: %s", TimeStringUtils.getTimeString(count)));
                adminSidebar.updateLine("timer", String.format("Heading to Podium: %s", TimeStringUtils.getTimeString(count)));
                count--;
            }
        }.runTaskTimer(plugin, 0L, 20L).getTaskId();
    }
    
    private void startVoting() {
        currentState = EventState.VOTING;
        List<GameType> votingPool = new ArrayList<>(List.of(GameType.values()));
        votingPool.removeAll(playedGames);
        for (Player participant : participants) {
            sidebar.removePlayer(participant);
        }
        for (Player admin : admins) {
            adminSidebar.removePlayer(admin);
        }
        voteManager.startVote(participants, votingPool, storageUtil.getVotingDuration(), this::startingGameDelay, admins);
        participants.clear();
        admins.clear();
    }
    
    private void startingGameDelay(GameType gameType) {
        currentState = EventState.DELAY;
        for (Player participant : gameManager.getOnlineParticipants()) {
            participants.add(participant);
            sidebar.addPlayer(participant);
        }
        for (Player admin : gameManager.getOnlineAdmins()) {
            admins.add(admin);
            adminSidebar.addPlayer(admin);
        }
        updateTeamScores();
        updatePersonalScores();
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
                    for (Player participant : participants) {
                        sidebar.removePlayer(participant);
                    }
                    for (Player admin : admins) {
                        adminSidebar.removePlayer(admin);
                    }
                    participants.clear();
                    admins.clear();
                    gameManager.startGame(gameType, Bukkit.getConsoleSender());
                    this.cancel();
                    return;
                }
                sidebar.updateLine("timer", String.format("%s: %s", gameType.getTitle(), TimeStringUtils.getTimeString(count)));
                adminSidebar.updateLine("timer", String.format("%s: %s", gameType.getTitle(), TimeStringUtils.getTimeString(count)));
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
        for (Player participant : gameManager.getOnlineParticipants()) {
            participants.add(participant);
            sidebar.addPlayer(participant);
        }
        for (Player admin : gameManager.getOnlineAdmins()) {
            admins.add(admin);
            adminSidebar.addPlayer(admin);
        }
        updateTeamScores();
        updatePersonalScores();
        playedGames.add(finishedGameType);
        currentGameNumber += 1;
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
                sidebar.updateLine("timer", String.format("Back to Hub: %s", TimeStringUtils.getTimeString(count)));
                adminSidebar.updateLine("timer", String.format("Back to Hub: %s", TimeStringUtils.getTimeString(count)));
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
                    if (identifyWinnersAndStartColossalColosseum()) {
                        currentState = EventState.PLAYING_GAME;
                    } else {
                        messageAllAdmins(Component.text("Unable to start Colossal Colosseum."));
                    }
                    this.cancel();
                    return;
                }
                sidebar.updateLine("timer", String.format("Colossal Colosseum: %s", TimeStringUtils.getTimeString(count)));
                adminSidebar.updateLine("timer", String.format("Colossal Colosseum: %s", TimeStringUtils.getTimeString(count)));
                count--;
            }
        }.runTaskTimer(plugin, 0L, 20L).getTaskId();
    }
    
    /**
     * @return true if two teams were picked and Colossal Colosseum started successfully. False if anything went wrong.
     */
    private boolean identifyWinnersAndStartColossalColosseum() {
        Set<String> allTeams = gameManager.getTeamNames();
        if (allTeams.size() < 2) {
            messageAllAdmins(Component.empty()
                    .append(Component.text("There are fewer than two teams online. Use "))
                    .append(Component.text("/mct game finalgame <first> <second>")
                            .clickEvent(ClickEvent.suggestCommand("/mct game finalgame "))
                            .decorate(TextDecoration.BOLD))
                    .append(Component.text(" to start the final game with the two chosen teams.")));
            return false;
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
            return false;
        }
        if (firstPlaces.length > 2) {
            messageAllAdmins(Component.text("There are more than 2 teams tied for first place. A tie breaker is needed. Use ")
                    .append(Component.text("/mct game finalgame <first> <second>")
                            .clickEvent(ClickEvent.suggestCommand("/mct game finalgame "))
                            .decorate(TextDecoration.BOLD))
                    .append(Component.text(" to start the final game with the two chosen teams."))
                    .color(NamedTextColor.RED));
            return false;
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
            return false;
        }
        String secondPlace = secondPlaces[0];
        for (Player participant : participants) {
            sidebar.removePlayer(participant);
        }
        for (Player admin : admins) {
            adminSidebar.removePlayer(admin);
        }
        startColossalColosseum(Bukkit.getConsoleSender(), firstPlace, secondPlace);
        participants.clear();
        admins.clear();
        return true;
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
        List<Player> participantPool;
        if (eventIsActive()) {
            participantPool = new ArrayList<>(participants);
        } else {
            participantPool = new ArrayList<>(gameManager.getOnlineParticipants());
        }
        for (Player participant : participantPool) {
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
    
        gameManager.removeParticipantsFromHub(participantPool);
        colossalColosseumGame.start(firstPlaceParticipants, secondPlaceParticipants, spectators, admins);
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
            Component message = Component.text("No winner declared.");
            messageAllAdmins(message);
            gameManager.messageOnlineParticipants(message);
            if (currentState != null) {
                startWaitingInHub();
            }
            return;
        }
        NamedTextColor teamColor = gameManager.getTeamNamedTextColor(winningTeam);
        Component formattedTeamDisplayName = gameManager.getFormattedTeamDisplayName(winningTeam);
        if (currentState == null) {
            Component message = Component.empty()
                    .append(formattedTeamDisplayName)
                    .append(Component.text(" wins!"))
                    .color(teamColor)
                    .decorate(TextDecoration.BOLD);
            messageAllAdmins(message);
            gameManager.messageOnlineParticipants(message);
            return;
        }
        Bukkit.getServer().sendMessage(Component.empty()
                .append(formattedTeamDisplayName)
                .append(Component.text(" wins ")
                    .append(Component.text(storageUtil.getTitle()))
                    .append(Component.text("!")))
                .color(teamColor)
                .decorate(TextDecoration.BOLD));
        toPodiumDelay(winningTeam);
    }
    
    @EventHandler
    public void onPlayerDamage(EntityDamageEvent event) {
        if (currentState == null) {
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
        if (currentState.equals(EventState.DELAY)) {
            event.setCancelled(true);
        }
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
    
    private void initializeAdminSidebar() {
        List<String> sortedTeamNames = sortTeamNames(gameManager.getTeamNames());
        numberOfTeams = sortedTeamNames.size();
        KeyLine[] teamLines = new KeyLine[numberOfTeams];
        for (int i = 0; i < numberOfTeams; i++) {
            String teamName = sortedTeamNames.get(i);
            String teamDisplayName = gameManager.getTeamDisplayName(teamName);
            ChatColor teamChatColor = gameManager.getTeamChatColor(teamName);
            int teamScore = gameManager.getScore(teamName);
            teamLines[i] = new KeyLine("team"+i, String.format("%s%s: %s", teamChatColor, teamDisplayName, teamScore));
        }
        adminSidebar.addLines(teamLines);
        adminSidebar.addLine("timer", "");
        adminSidebar.updateTitle(storageUtil.getTitle());
    }
    
    private void clearAdminSidebar() {
        adminSidebar.updateTitle(Sidebar.DEFAULT_TITLE);
        adminSidebar.removeAllPlayers();
        adminSidebar.deleteAllLines();
        adminSidebar = null;
    }
    
    private void initializeSidebar() {
        List<String> sortedTeamNames = sortTeamNames(gameManager.getTeamNames());
        numberOfTeams = sortedTeamNames.size();
        KeyLine[] teamLines = new KeyLine[numberOfTeams];
        for (int i = 0; i < numberOfTeams; i++) {
            String teamName = sortedTeamNames.get(i);
            String teamDisplayName = gameManager.getTeamDisplayName(teamName);
            ChatColor teamChatColor = gameManager.getTeamChatColor(teamName);
            int teamScore = gameManager.getScore(teamName);
            teamLines[i] = new KeyLine("team"+i, String.format("%s%s: %s", teamChatColor, teamDisplayName, teamScore));
        }
        sidebar.addLines(teamLines);
        sidebar.addLine("personalScore", "");
        sidebar.addLine("timer", "");
        sidebar.updateTitle(storageUtil.getTitle());
        updatePersonalScores();
    }
    
    private void clearSidebar() {
        sidebar.updateTitle(Sidebar.DEFAULT_TITLE);
        sidebar.removeAllPlayers();
        sidebar.deleteAllLines();
        sidebar = null;
    }
    
    public void updateTeamScores() {
        if (sidebar == null) {
            return;
        }
        List<String> sortedTeamNames = sortTeamNames(gameManager.getTeamNames());
        if (numberOfTeams != sortedTeamNames.size()) {
            reorderTeamLines(sortedTeamNames);
            return;
        }
        KeyLine[] teamLines = new KeyLine[numberOfTeams];
        for (int i = 0; i < numberOfTeams; i++) {
            String teamName = sortedTeamNames.get(i);
            String teamDisplayName = gameManager.getTeamDisplayName(teamName);
            ChatColor teamChatColor = gameManager.getTeamChatColor(teamName);
            int teamScore = gameManager.getScore(teamName);
            teamLines[i] = new KeyLine("team"+i, String.format("%s%s: %s", teamChatColor, teamDisplayName, teamScore));
        }
        sidebar.updateLines(teamLines);
        adminSidebar.updateLines(teamLines);
    }
    
    private void reorderTeamLines(List<String> sortedTeamNames) {
        String[] teamKeys = new String[numberOfTeams];
        for (int i = 0; i < numberOfTeams; i++) {
            teamKeys[i] = "team"+i;
        }
        sidebar.deleteLines(teamKeys);
        adminSidebar.deleteLines(teamKeys);
        
        numberOfTeams = sortedTeamNames.size();
        KeyLine[] teamLines = new KeyLine[numberOfTeams];
        for (int i = 0; i < numberOfTeams; i++) {
            String teamName = sortedTeamNames.get(i);
            String teamDisplayName = gameManager.getTeamDisplayName(teamName);
            ChatColor teamChatColor = gameManager.getTeamChatColor(teamName);
            int teamScore = gameManager.getScore(teamName);
            teamLines[i] = new KeyLine("team"+i, String.format("%s%s: %s", teamChatColor, teamDisplayName, teamScore));
        }
        sidebar.addLines(0, teamLines);
        adminSidebar.addLines(0, teamLines);
    }
    
    private void updatePersonalScores() {
        for (Player participant : participants) {
            int score = gameManager.getScore(participant.getUniqueId());
            String contents = String.format("%sPoints: %s", ChatColor.GOLD, score);
            updatePersonalScore(participant, contents);
        }
    }
    
    public void updatePersonalScore(Player participant, String contents) {
        if (sidebar == null) {
            return;
        }
        if (!participants.contains(participant)) {
            return;
        }
        sidebar.updateLine(participant.getUniqueId(), "personalScore", contents);
    }
    
    protected List<String> sortTeamNames(Set<String> teamNames) {
        List<String> sortedTeamNames = new ArrayList<>(teamNames);
        sortedTeamNames.sort(Comparator.comparing(gameManager::getScore, Comparator.reverseOrder()));
        sortedTeamNames.sort(Comparator
                .comparing(teamName -> gameManager.getScore((String) teamName))
                .reversed()
                .thenComparing(teamName -> ((String) teamName))
        );
        return sortedTeamNames;
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
