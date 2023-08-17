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
import org.braekpo1nt.mctmanager.hub.HubManager;
import org.braekpo1nt.mctmanager.ui.TimeStringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class EventManager {
    
    private final Main plugin;
    private final GameManager gameManager;
    private final VoteManager voteManager;
    private final HubManager hubManager;
    private final ColossalColosseumGame colossalColosseumGame;
    private final Map<GameType, ScoreKeeper> scoreKeepers = new HashMap<>();
    private int currentGameNumber = 0;
    private int maxGames = 6;
    private int finalGameEndTaskId;
    private int fiveMinuteBreakTaskId;
    private EventState currentState;
    private EventState lastState;
    private int hubTimerTaskId;
    private boolean hubTimerPaused = false;
    
    public EventManager(Main plugin, GameManager gameManager, VoteManager voteManager, HubManager hubManager) {
        this.plugin = plugin;
        this.gameManager = gameManager;
        this.voteManager = voteManager;
        this.hubManager = hubManager;
        this.colossalColosseumGame = new ColossalColosseumGame(plugin, gameManager);
    }
    
    public void startEvent(CommandSender sender, int maxGames) {
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
        currentGameNumber = 1;
        this.maxGames = maxGames;
        gameManager.clearPlayedGames();
        scoreKeepers.clear();
        messageAllAdmins(Component.text("Starting event. On game ")
                .append(Component.text(currentGameNumber))
                .append(Component.text("/"))
                .append(Component.text(maxGames))
                .append(Component.text(".")));
        hubManager.returnParticipantsToHub(gameManager.getOnlineParticipants());
        startWaitingInHub();
    }
    
    public void stopEvent(CommandSender sender) {
        if (currentState == null) {
            sender.sendMessage(Component.text("There is no event running.")
                    .color(NamedTextColor.RED));
            return;
        }
        Component message = Component.text("Ending event. ")
                .append(Component.text(currentGameNumber - 1))
                .append(Component.text("/"))
                .append(Component.text(maxGames))
                .append(Component.text(" games were played."));
        messageAllAdmins(message);
        Bukkit.getLogger().info(String.format("Ending event. %d/%d games were played", currentGameNumber - 1, maxGames));
        cancelAllTasks();
        currentState = null;
        currentGameNumber = 0;
        this.maxGames = 6;
    }
    
    public void pauseEvent(CommandSender sender) {
        if (currentState == null) {
            sender.sendMessage(Component.text("There is no event running.")
                    .color(NamedTextColor.RED));
            return;
        }
        if (gameManager.getActiveGame() != null || gameManager.isStartingGameWithDelay()) {
            sender.sendMessage(Component.text("You can't pause the event while a game is active.")
                    .color(NamedTextColor.RED));
            return;
        }
        if (voteManager.isVoting()) {
            sender.sendMessage(Component.text("You can't pause the event during the voting phase.")
                    .color(NamedTextColor.RED));
            return;
        }
        lastState = currentState;
        currentState = EventState.PAUSED;
        hubTimerPaused = true;
        Component pauseMessage = Component.text("Event paused.");
        sender.sendMessage(pauseMessage);
        messageAllAdmins(pauseMessage);
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
        currentState = lastState;
        lastState = null;
        hubTimerPaused = false;
        Component resumeMessage = Component.text("Event resumed.");
        sender.sendMessage(resumeMessage);
        messageAllAdmins(resumeMessage);
    }
    
    /**
     * Subtracts the scores accumulated during the provided game from all teams and players
     * @param sender The sender
     * @param gameType The game to undo
     */
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
        List<GameType> playedGames = gameManager.getPlayedGames();
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
        sender.sendMessage(reportBuilder.build());
        Bukkit.getConsoleSender().sendMessage(reportBuilder.build());
    }
    
    /**
     * 
     * @param gameType The game that just ended
     */
    public void gameIsOver(GameType gameType) {
        if (currentState == null) {
            return;
        }
        gameManager.addPlayedGame(gameType);
        currentGameNumber++;
        messageAllAdmins(Component.text("Now on game ")
                .append(Component.text(currentGameNumber))
                .append(Component.text("/"))
                .append(Component.text(maxGames))
                .append(Component.text(".")));
    }
    
    private void startWaitingInHub() {
        currentState = EventState.WAITING_HUB;
        hubTimerPaused = false;
        String currentGameString = String.format("Game %d/%d", currentGameNumber, maxGames);
        for (Player participant : gameManager.getOnlineParticipants()) {
            initializeHubTimerDisplay(participant, currentGameString);
        }
        this.hubTimerTaskId = new BukkitRunnable() {
            int count = 20;
            @Override
            public void run() {
                if (hubTimerPaused) {
                    return;
                }
                if (count <= 0) {
                    startVote();
                    this.cancel();
                    return;
                }
                String timeString = TimeStringUtils.getTimeString(count);
                for (Player participant : gameManager.getOnlineParticipants()) {
                    updateHubTimerDisplay(participant, timeString);
                }
                count--;
            }
        }.runTaskTimer(plugin, 0L, 20L).getTaskId();
    }
    
    private void start5MinuteBreak() {
        hubTimerPaused = false;
        String currentGameString = String.format("Game %d/%d", currentGameNumber, maxGames);
        for (Player participant : gameManager.getOnlineParticipants()) {
            initializeFiveMinuteBreakDisplay(participant, currentGameString);
        }
        messageAllParticipants(Component.text("Break time")
                .decorate(TextDecoration.BOLD)
                .color(NamedTextColor.YELLOW));
        this.fiveMinuteBreakTaskId = new BukkitRunnable() {
            int count = 60*5;
            @Override
            public void run() {
                if (hubTimerPaused) {
                    return;
                }
                if (count <= 0) {
                    messageAllParticipants(Component.text("Break is over")
                            .decorate(TextDecoration.BOLD)
                            .color(NamedTextColor.YELLOW));
                    startWaitingInHub();
                    this.cancel();
                    return;
                }
                String timeString = ChatColor.YELLOW+TimeStringUtils.getTimeString(count);
                for (Player participant : gameManager.getOnlineParticipants()) {
                    updateFiveMinuteBreakDisplay(participant, timeString);
                }
                count--;
            }
        }.runTaskTimer(plugin, 0L, 20L).getTaskId();
    }
    
    /**
     * Starts the voting phase for the event
     */
    public void startVote() {
        if (currentGameNumber > maxGames) {
            messageAllAdmins(Component.text("All games have been played. Initiating final game with top two teams."));
            kickOffFinalGame();
            return;
        }
        
        List<GameType> votingPool = new ArrayList<>(List.of(GameType.values()));
        votingPool.removeAll(gameManager.getPlayedGames());
        
        if (votingPool.isEmpty()) {
            messageAllAdmins(Component.text("No more games to play. Manually initiate final game."));
            return;
        }
        voteManager.startVote(gameManager.getOnlineParticipants(), votingPool);
    }
    
    public void finalGameIsOver(String winningTeamName) {
        Bukkit.getLogger().info("Final game is over");
        finalGameEndTaskId = new BukkitRunnable() {
            @Override
            public void run() {
                String winningTeam = gameManager.getTeamDisplayName(winningTeamName);
                List<Player> winningTeamParticipants = gameManager.getOnlinePlayersOnTeam(winningTeamName);
                ChatColor chatColor = gameManager.getTeamChatColor(winningTeamName);
                List<Player> otherParticipants = new ArrayList<>();
                for (Player player : gameManager.getOnlineParticipants()) {
                    if (!winningTeamParticipants.contains(player)) {
                        otherParticipants.add(player);
                    }
                }
                currentState = null;
                hubManager.sendParticipantsToPedestal(winningTeamParticipants, winningTeam, chatColor, otherParticipants);
            }
        }.runTaskLater(plugin, 5*20).getTaskId();
    }
    
    public void kickOffFinalGame() {
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
    
    public void cancelAllTasks() {
        Bukkit.getScheduler().cancelTask(this.finalGameEndTaskId);
        Bukkit.getScheduler().cancelTask(this.hubTimerTaskId);
        Bukkit.getScheduler().cancelTask(this.fiveMinuteBreakTaskId);
    }
    
    private void initializeHubTimerDisplay(Player participant, String gameString) {
        gameManager.getFastBoardManager().updateLines(
                participant.getUniqueId(),
                "",
                gameString,
                ""
        );
    }
    
    private void updateHubTimerDisplay(Player participant, String timeString) {
        gameManager.getFastBoardManager().updateLine(
                participant.getUniqueId(),
                2,
                timeString
        );
    }
    
    private void initializeFiveMinuteBreakDisplay(Player participant, String gameString) {
        gameManager.getFastBoardManager().updateLines(
                participant.getUniqueId(),
                "",
                gameString,
                ChatColor.YELLOW+"Break",
                ""
        );
    }
    
    private void updateFiveMinuteBreakDisplay(Player participant, String timeString) {
        gameManager.getFastBoardManager().updateLine(
                participant.getUniqueId(),
                3,
                timeString
        );
    }
    
    /**
     * Check if half the games have been played
     * @return true if the currentGameNumber-1 is half of the maxGames. False if it is lower or higher. 
     * If maxGames is odd, it must be the greater half (i.e. 2 is half of 3, 1 is not). 
     */
    public boolean halfOfEventHasBeenPlayed() {
        double half = maxGames / 2.0;
        return half <= currentGameNumber-1 && currentGameNumber-1 <= Math.ceil(half);
    }
    
    /**
     * @return true if an event is active, false if not
     */
    public boolean eventIsActive() {
        return currentState != null;
    }
    
    private void messageAllParticipants(Component message) {
        for (Player participant : gameManager.getOnlineParticipants()) {
            participant.sendMessage(message);
        }
    }
    
    private void messageAllAdmins(Component message) {
        gameManager.messageAdmins(message);
    }
    
    
    // Testing
    public int getMaxGames() {
        return maxGames;
    }
    
}
