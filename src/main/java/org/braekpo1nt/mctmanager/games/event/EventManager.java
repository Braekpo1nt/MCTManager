package org.braekpo1nt.mctmanager.games.event;

import lombok.Data;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.braekpo1nt.mctmanager.games.colossalcombat.ColossalCombatGame;
import org.braekpo1nt.mctmanager.games.event.config.EventConfig;
import org.braekpo1nt.mctmanager.games.event.config.EventConfigController;
import org.braekpo1nt.mctmanager.games.event.states.*;
import org.braekpo1nt.mctmanager.games.game.enums.GameType;
import org.braekpo1nt.mctmanager.games.voting.VoteManager;
import org.braekpo1nt.mctmanager.ui.sidebar.KeyLine;
import org.braekpo1nt.mctmanager.ui.sidebar.Sidebar;
import org.braekpo1nt.mctmanager.ui.timer.TimerManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

@Data
public class EventManager implements Listener {
    private @NotNull EventState state;
    
    private final Main plugin;
    private final GameManager gameManager;
    private final VoteManager voteManager;
    private final ColossalCombatGame colossalCombatGame;
    private final EventConfigController configController;
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
    private final ItemStack crown = new ItemStack(Material.CARVED_PUMPKIN);
    private final TimerManager timerManager;
    private Sidebar sidebar;
    private Sidebar adminSidebar;
    private int numberOfTeams = 0;
    private EventConfig config;
    private int maxGames = 6;
    private int currentGameNumber = 0;
    private List<Player> participants = new ArrayList<>();
    private List<Player> admins = new ArrayList<>();
    private @Nullable String winningTeam;
    
    public EventManager(Main plugin, GameManager gameManager, VoteManager voteManager) {
        this.plugin = plugin;
        this.timerManager = new TimerManager(plugin);
        this.gameManager = gameManager;
        this.voteManager = voteManager;
        this.configController = new EventConfigController(plugin.getDataFolder());
        this.colossalCombatGame = new ColossalCombatGame(plugin, gameManager);
        this.crown.editMeta(meta -> meta.setCustomModelData(1));
        this.state = new OffState(this);
    }
    
    /**
     * Add the participants back to the {@link EventManager#participants} list and the {@link EventManager#sidebar}, add the admins back to the {@link EventManager#admins} list and the {@link EventManager#adminSidebar}, and update the scores on all sidebars.
     */
    public void initializeParticipantsAndAdmins() {
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
        sidebar.updateLine("currentGame", getCurrentGameLine());
        adminSidebar.updateLine("currentGame", getCurrentGameLine());
    }
    
    public void updatePersonalScores() {
        for (Player participant : participants) {
            int score = gameManager.getScore(participant.getUniqueId());
            String contents = String.format("%sPersonal: %s", ChatColor.GOLD, score);
            updatePersonalScore(participant, contents);
        }
    }
    
    /**
     * @return a line for sidebars saying what the current game is
     */
    public String getCurrentGameLine() {
        if (currentGameNumber <= maxGames) {
            return String.format("Game [%d/%d]", currentGameNumber, maxGames);
        }
        if (state instanceof PodiumState) {
            return "Thanks for playing!";
        }
        return "Final Round";
    }
    
    public boolean colossalCombatIsActive() {
        return colossalCombatGame.isActive();
    }
    
    public void onParticipantJoin(Player participant) {
        state.onParticipantJoin(participant);
    }
    
    public void onParticipantQuit(Player participant) {
        state.onParticipantQuit(participant);
    }
    
    public void onAdminJoin(Player admin) {
        state.onAdminJoin(admin);
    }
    
    public void onAdminQuit(Player admin) {
        state.onAdminQuit(admin);
    }
    
    public void startEvent(CommandSender sender, int numberOfGames) {
        state.startEvent(sender, numberOfGames);
    }
    
    public void stopEvent(CommandSender sender) {
        if (state instanceof OffState) {
            sender.sendMessage(Component.text("There is no event running.")
                    .color(NamedTextColor.RED));
            return;
        }
        HandlerList.unregisterAll(this);
        this.setState(new OffState(this));
        Component message = Component.text("Ending event. ")
                .append(Component.text(currentGameNumber - 1))
                .append(Component.text("/"))
                .append(Component.text(maxGames))
                .append(Component.text(" games were played."));
        sender.sendMessage(message);
        messageAllAdmins(message);
        Bukkit.getLogger().info(String.format("Ending event. %d/%d games were played", currentGameNumber - 1, maxGames));
        if (colossalCombatGame.isActive()) {
            colossalCombatGame.stop(null);
        }
        if (winningTeam != null) {
            for (Player participant : participants) {
                String team = gameManager.getTeamName(participant.getUniqueId());
                if (team.equals(winningTeam)) {
                    removeCrown(participant);
                }
            }e
        }
        clearSidebar();
        clearAdminSidebar();
        participants.clear();
        admins.clear();
        cancelAllTasks();
        scoreKeepers.clear();
        currentGameNumber = 0;
        maxGames = 6;
        winningTeam = null;
    }
    
    private void clearSidebar() {
        sidebar.updateTitle(Sidebar.DEFAULT_TITLE);
        sidebar.removeAllPlayers();
        sidebar.deleteAllLines();
        sidebar = null;
    }
    
    private void clearAdminSidebar() {
        adminSidebar.updateTitle(Sidebar.DEFAULT_TITLE);
        adminSidebar.removeAllPlayers();
        adminSidebar.deleteAllLines();
        adminSidebar = null;
    }
    
    public void undoGame(@NotNull CommandSender sender, @NotNull GameType gameType, int iterationIndex) {
        if (state instanceof OffState) {
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
    
    public void addGameToVotingPool(@NotNull CommandSender sender, @NotNull GameType gameToAdd) {
        if (state instanceof OffState) {
            sender.sendMessage(Component.text("There isn't an event going on.")
                    .color(NamedTextColor.RED));
            return;
        }
        if (!playedGames.contains(gameToAdd)) {
            sender.sendMessage(Component.text("This game is already in the voting pool.")
                    .color(NamedTextColor.YELLOW));
            return;
        }
        if (state instanceof VotingState) {
            sender.sendMessage(Component.text("Can't modify the voting pool mid-vote.")
                    .color(NamedTextColor.RED));
            return;
        }
        playedGames.remove(gameToAdd);
        sender.sendMessage(Component.text(gameToAdd.getTitle())
                .append(Component.text(" has been added to the voting pool.")));
    }
    
    public void removeGameFromVotingPool(@NotNull CommandSender sender, @NotNull GameType gameToRemove) {
        if (state instanceof OffState) {
            sender.sendMessage(Component.text("There isn't an event going on.")
                    .color(NamedTextColor.RED));
            return;
        }
        if (state instanceof VotingState) {
            sender.sendMessage(Component.text("Can't modify the voting pool mid-vote.")
                    .color(NamedTextColor.RED));
            return;
        }
        if (playedGames.contains(gameToRemove)) {
            sender.sendMessage(Component.text("This game is not in the voting pool.")
                    .color(NamedTextColor.RED));
            return;
        }
        playedGames.add(gameToRemove);
        sender.sendMessage(Component.text(gameToRemove.getTitle())
                .append(Component.text(" has been removed from the voting pool")));
    }
    
    /**
     * For use with the undo operation. Gets the number of times a game has been played this round.
     * @param gameType the game to check for the iterations of
     * @return the game iterations (the number of times a game has been played this event).
     * -1 if an event isn't active, 0 if the gameType hasn't been played yet
     */
    public int getGameIterations(GameType gameType) {
        if (state instanceof OffState) {
            return -1;
        }
        if (!scoreKeepers.containsKey(gameType)) {
            return 0;
        }
        return scoreKeepers.get(gameType).size();
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
    
    public void readyUpParticipant(@NotNull Player participant) {
        if (state instanceof OffState) {
            participant.sendMessage(Component.text("There is no event going on right now"));
            return;
        }
        if (!(state instanceof ReadyUpState readyUpState)) {
            // do nothing
            return;
        }
        readyUpState.readyUpParticipant(participant);
    }
    
    public void unReadyParticipant(@NotNull Player participant) {
        /*
         * this method is hyper state-specific. Don't use this as an example for how to
         * properly implement the State design pattern
         */
        if (state instanceof OffState) {
            participant.sendMessage(Component.text("There is no event going on right now"));
            return;
        }
        if (!(state instanceof ReadyUpState readyUpState)) {
            // do nothing
            return;
        }
        readyUpState.unReadyParticipant(participant);
    }
    
    @EventHandler
    public void onPlayerDamage(EntityDamageEvent event) {
        if (event.getCause().equals(EntityDamageEvent.DamageCause.VOID)) {
            return;
        }
        if (!(event.getEntity() instanceof Player participant)) {
            return;
        }
        if (!participants.contains(participant)) {
            return;
        }
        state.onPlayerDamage(event);
    }
    
    @EventHandler
    public void onClickInventory(InventoryClickEvent event) {
        Player participant = ((Player) event.getWhoClicked());
        if (!participants.contains(participant)) {
            return;
        }
        state.onClickInventory(event);
    }
    
    @EventHandler
    public void onDropItem(PlayerDropItemEvent event) {
        Player participant = event.getPlayer();
        if (!participants.contains(participant)) {
            return;
        }
        state.onDropItem(event);
    }
    
    /**
     * To be called when a game is over. The GameManager knows when a game ends, 
     * and thus can call this method if an event is running.
     * @param finishedGameType The GameType of the game that just ended
     */
    public void gameIsOver(GameType finishedGameType) {
        state.gameIsOver(finishedGameType);
    }
    
    public void colossalCombatIsOver(String winningTeam) {
        state.colossalCombatIsOver(winningTeam);
    }
    
    public void stopColossalCombat(@NotNull CommandSender sender) {
        state.stopColossalCombat(sender);
    }
    
    public void startColossalCombat(@NotNull CommandSender sender, @NotNull String firstTeam, @NotNull String secondTeam) {
        state.startColossalCombat(sender, firstTeam, secondTeam);
    }
    
    public boolean eventIsActive() {
        return !(state instanceof OffState);
    }
    
    public void cancelAllTasks() {
        voteManager.cancelVote();
        timerManager.cancel();
    }
    
    public void giveCrown(Player participant) {
        participant.getInventory().setHelmet(crown);
    }
    
    public void removeCrown(Player participant) {
        ItemStack helmet = participant.getInventory().getHelmet();
        if (helmet != null && helmet.equals(crown)) {
            participant.getInventory().setHelmet(null);
        }
    }
    
    /**
     * @return true if the game number should be displayed in-game
     */
    public boolean shouldDisplayGameNumber() {
        return config.shouldDisplayGameNumber();
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
    
    /**
     * Assigns a value to the {@link #maxGames} field. This should not be used unless you know
     * what you're doing. Instead, use {@link #modifyMaxGames(CommandSender, int)}
     * @param maxGames the value to set the maxGames to
     * @see #modifyMaxGames(CommandSender, int) 
     */
    public void setMaxGames(int maxGames) {
        this.maxGames = maxGames;
    }
    
    /**
     * This is used to change the maxGames of the event. This calls state-specific
     * behavior and can safely be used at any time without causing any errors. The state
     * may or may not ignore the value with a message sent to the sender.
     * @param sender the sender of the modify command
     * @param newMaxGames the number to set the max games to
     */
    public void modifyMaxGames(@NotNull CommandSender sender, int newMaxGames) {
        state.setMaxGames(sender, newMaxGames);
    }
    
    /**
     * The nth multiplier is used on the nth game in the event. If there are x multipliers, and we're on game z where z is greater than x, the xth multiplier is used.
     * @return a multiplier for the score based on the progression in the match.
     */
    public double matchProgressPointMultiplier() {
        if (currentGameNumber <= 0) {
            return 1;
        }
        double[] multipliers = config.getMultipliers();
        if (currentGameNumber > multipliers.length) {
            return multipliers[multipliers.length - 1];
        }
        return multipliers[currentGameNumber - 1];
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
            teamLines[i] = new KeyLine("team"+i, String.format("%s%s: %s%s", teamChatColor, teamDisplayName, ChatColor.GOLD, teamScore));
        }
        sidebar.updateLines(teamLines);
        if (adminSidebar == null) {
            return;
        }
        adminSidebar.updateLines(teamLines);
    }
    
    public List<String> sortTeamNames(Set<String> teamNames) {
        List<String> sortedTeamNames = new ArrayList<>(teamNames);
        sortedTeamNames.sort(Comparator.comparing(gameManager::getScore, Comparator.reverseOrder()));
        sortedTeamNames.sort(Comparator
                .comparing(teamName -> gameManager.getScore((String) teamName))
                .reversed()
                .thenComparing(teamName -> ((String) teamName))
        );
        return sortedTeamNames;
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
            teamLines[i] = new KeyLine("team"+i, String.format("%s%s: %s%s", teamChatColor, teamDisplayName, ChatColor.GOLD, teamScore));
        }
        sidebar.addLines(0, teamLines);
        adminSidebar.addLines(0, teamLines);
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
    
    /**
     * Track the points earned for the given team in the given game. 
     * If the event is not active, nothing happens.
     * @param teamName The team to track points for
     * @param points the points to add
     * @param gameType the game that the points came from
     */
    public void trackPoints(String teamName, int points, GameType gameType) {
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
        List<ScoreKeeper> iterationScoreKeepers = scoreKeepers.get(gameType);
        ScoreKeeper iteration = iterationScoreKeepers.get(iterationScoreKeepers.size() - 1);
        iteration.addPoints(participantUUID, points);
    }
    
    public void messageAllAdmins(Component message) {
        gameManager.messageAdmins(message);
    }
    
    public boolean allGamesHaveBeenPlayed() {
        return currentGameNumber >= maxGames + 1;
    }
}
