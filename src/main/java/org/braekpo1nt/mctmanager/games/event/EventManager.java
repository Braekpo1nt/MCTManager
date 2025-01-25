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
import org.braekpo1nt.mctmanager.games.utils.GameManagerUtils;
import org.braekpo1nt.mctmanager.games.voting.VoteManager;
import org.braekpo1nt.mctmanager.participant.Participant;
import org.braekpo1nt.mctmanager.participant.Team;
import org.braekpo1nt.mctmanager.ui.sidebar.KeyLine;
import org.braekpo1nt.mctmanager.ui.sidebar.Sidebar;
import org.braekpo1nt.mctmanager.ui.timer.TimerManager;
import org.braekpo1nt.mctmanager.ui.topbar.ReadyUpTopbar;
import org.bukkit.Bukkit;
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
    private Map<UUID, Participant> participants = new HashMap<>();
    private List<Player> admins = new ArrayList<>();
    private @Nullable String winningTeam;
    private final ReadyUpManager readyUpManager = new ReadyUpManager();
    private final ReadyUpTopbar topbar = new ReadyUpTopbar();
    
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
        for (Participant participant : gameManager.getOnlineParticipants()) {
            participants.put(participant.getUniqueId(), participant);
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
        for (Participant participant : participants.values()) {
            int score = gameManager.getScore(participant.getUniqueId());
            updatePersonalScore(participant, Component.empty()
                    .append(Component.text("Personal: "))
                    .append(Component.text(score))
                    .color(NamedTextColor.GOLD)
            );
        }
    }
    
    /**
     * @return a line for sidebars saying what the current game is
     */
    public Component getCurrentGameLine() {
        if (currentGameNumber <= maxGames) {
            return Component.empty()
                    .append(Component.text("Game ["))
                    .append(Component.text(currentGameNumber))
                    .append(Component.text("/"))
                    .append(Component.text(maxGames))
                    .append(Component.text("]"));
        }
        if (state instanceof PodiumState) {
            return Component.text("Thanks for playing!");
        }
        return Component.text("Final Round");
    }
    
    public boolean colossalCombatIsActive() {
        return colossalCombatGame.isActive();
    }
    
    public void onParticipantJoin(Participant participant) {
        state.onParticipantJoin(participant);
    }
    
    public void onParticipantQuit(Participant participant) {
        state.onParticipantQuit(participant);
    }
    
    public void onAdminJoin(Player admin) {
        state.onAdminJoin(admin);
    }
    
    public void onAdminQuit(Player admin) {
        state.onAdminQuit(admin);
    }
    
    public void startEvent(CommandSender sender, int numberOfGames, int currentGameNumber) {
        state.startEvent(sender, numberOfGames, currentGameNumber);
    }
    
    public void stopEvent(CommandSender sender) {
        if (state instanceof OffState) {
            sender.sendMessage(Component.text("There is no event running.")
                    .color(NamedTextColor.RED));
            return;
        }
        HandlerList.unregisterAll(this);
        cancelAllTasks();
        this.setState(new OffState(this));
        Component message = Component.text("Ending event. ")
                .append(Component.text(currentGameNumber - 1))
                .append(Component.text("/"))
                .append(Component.text(maxGames))
                .append(Component.text(" games were played."));
        sender.sendMessage(message);
        messageAllAdmins(message);
        Main.logger().info(String.format("Ending event. %d/%d games were played", currentGameNumber - 1, maxGames));
        if (colossalCombatGame.isActive()) {
            colossalCombatGame.stop(null);
        }
        if (winningTeam != null) {
            for (Participant participant : participants.values()) {
                if (participant.getTeamId().equals(winningTeam)) {
                    removeCrown(participant);
                }
            }
        }
        clearSidebar();
        clearAdminSidebar();
        topbar.hideAllPlayers();
        topbar.removeAllTeams();
        participants.clear();
        admins.clear();
        readyUpManager.clear();
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
        Collection<Team> teams = gameManager.getTeams();
        for (Team team : teams) {
            int teamScoreToSubtract = scoreKeeper.getScore(team.getTeamId());
            int teamCurrentScore = gameManager.getScore(team.getTeamId());
            if (teamCurrentScore - teamScoreToSubtract < 0) {
                teamScoreToSubtract = teamCurrentScore;
            }
            gameManager.addScore(team, -teamScoreToSubtract);
            
            Collection<UUID> participantUUIDs = team.getMemberUUIDs();
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
        Collection<Team> teams = gameManager.getTeams();
        TextComponent.Builder reportBuilder = Component.text()
                .append(Component.text("|Scores for ("))
                .append(Component.text(gameType.getTitle())
                        .decorate(TextDecoration.BOLD))
                .append(Component.text("):\n"))
                .color(NamedTextColor.YELLOW);
        for (Team team : teams) {
            int teamScoreToSubtract = scoreKeeper.getScore(team.getTeamId());
            reportBuilder.append(Component.text("|  - "))
                    .append(team.getFormattedDisplayName())
                    .append(Component.text(": "))
                    .append(Component.text(teamScoreToSubtract)
                            .color(NamedTextColor.GOLD)
                            .decorate(TextDecoration.BOLD))
                    .append(Component.text("\n"));
            
            Collection<UUID> participantUUIDs = team.getMemberUUIDs();
            for (UUID participantUUID : participantUUIDs) {
                Player participant = Bukkit.getPlayer(participantUUID);
                if (participant != null) {
                    int participantScoreToSubtract = scoreKeeper.getScore(participantUUID);
                    reportBuilder.append(Component.text("|    - "))
                            .append(Component.text(participant.getName())
                                    .color(team.getColor()))
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
    
    public void readyUpParticipant(@NotNull UUID uuid) {
        Participant participant = participants.get(uuid);
        if (participant == null) {
            return;
        }
        state.readyUpParticipant(participant);
    }
    
    public void unReadyParticipant(@NotNull UUID uuid) {
        Participant participant = participants.get(uuid);
        if (participant == null) {
            return;
        }
        state.unReadyParticipant(participant);
    }
    
    public void listReady(@NotNull CommandSender sender, @Nullable String teamId) {
        state.listReady(sender, teamId);
    }
    
    @EventHandler
    public void onPlayerDamage(EntityDamageEvent event) {
        if (GameManagerUtils.EXCLUDED_CAUSES.contains(event.getCause())) {
            return;
        }
        if (!(event.getEntity() instanceof Player participant)) {
            return;
        }
        if (!participants.containsKey(participant.getUniqueId())) {
            return;
        }
        state.onPlayerDamage(event);
    }
    
    @EventHandler
    public void onClickInventory(InventoryClickEvent event) {
        Player participant = ((Player) event.getWhoClicked());
        if (!participants.containsKey(participant.getUniqueId())) {
            return;
        }
        state.onClickInventory(event);
    }
    
    @EventHandler
    public void onDropItem(PlayerDropItemEvent event) {
        Player participant = event.getPlayer();
        if (!participants.containsKey(participant.getUniqueId())) {
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
        state.cancelAllTasks();
    }
    
    /**
     * @return the event title from the config if the event is active,
     * or the {@link Sidebar#DEFAULT_TITLE} if not
     */
    public @NotNull Component getTitle() {
        if (eventIsActive()) {
            return config.getTitle();
        } else {
            return Sidebar.DEFAULT_TITLE;
        }
    }
    
    public void giveCrown(Participant participant) {
        participant.getInventory().setHelmet(crown);
    }
    
    public void removeCrown(Participant participant) {
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
        List<Team> sortedTeams = sortTeams(gameManager.getTeams());
        if (numberOfTeams != sortedTeams.size()) {
            reorderTeamLines(sortedTeams);
            return;
        }
        KeyLine[] teamLines = new KeyLine[numberOfTeams];
        for (int i = 0; i < numberOfTeams; i++) {
            Team team = sortedTeams.get(i);
            int teamScore = gameManager.getScore(team.getTeamId());
            teamLines[i] = new KeyLine("team"+i, Component.empty()
                    .append(team.getFormattedDisplayName())
                    .append(Component.text(": "))
                    .append(Component.text(teamScore)
                            .color(NamedTextColor.GOLD))
            );
        }
        sidebar.updateLines(teamLines);
        if (adminSidebar == null) {
            return;
        }
        adminSidebar.updateLines(teamLines);
    }
    
    public List<Team> sortTeams(Collection<Team> teamIds) {
        List<Team> sortedTeamIds = new ArrayList<>(teamIds);
        sortedTeamIds.sort(Comparator.comparing(team -> gameManager.getScore(team.getTeamId()), Comparator.reverseOrder()));
        sortedTeamIds.sort(Comparator
                .comparing(teamId -> gameManager.getScore((String) teamId))
                .reversed()
                .thenComparing(teamId -> ((String) teamId))
        );
        return sortedTeamIds;
    }
    
    private void reorderTeamLines(List<Team> sortedTeamIds) {
        String[] teamKeys = new String[numberOfTeams];
        for (int i = 0; i < numberOfTeams; i++) {
            teamKeys[i] = "team"+i;
        }
        sidebar.deleteLines(teamKeys);
        adminSidebar.deleteLines(teamKeys);
        
        numberOfTeams = sortedTeamIds.size();
        KeyLine[] teamLines = new KeyLine[numberOfTeams];
        for (int i = 0; i < numberOfTeams; i++) {
            Team team = sortedTeamIds.get(i);
            int teamScore = gameManager.getScore(team.getTeamId());
            teamLines[i] = new KeyLine("team"+i, Component.empty()
                    .append(team.getFormattedDisplayName())
                    .append(Component.text(": "))
                    .append(Component.text(teamScore)
                            .color(NamedTextColor.GOLD))
            );
        }
        sidebar.addLines(0, teamLines);
        adminSidebar.addLines(0, teamLines);
    }
    
    public void updatePersonalScore(Participant participant, Component contents) {
        if (sidebar == null) {
            return;
        }
        if (!participants.containsKey(participant.getUniqueId())) {
            return;
        }
        sidebar.updateLine(participant.getUniqueId(), "personalScore", contents);
    }
    
    /**
     * Track the points earned for the given team in the given game. 
     * If the event is not active, nothing happens.
     * @param teamId The team to track points for
     * @param points the points to add
     * @param gameType the game that the points came from
     */
    public void trackPoints(String teamId, int points, GameType gameType) {
        if (state instanceof OffState) {
            return;
        }
        List<ScoreKeeper> iterationScoreKeepers = scoreKeepers.get(gameType);
        ScoreKeeper iteration = iterationScoreKeepers.getLast();
        iteration.addPoints(teamId, points);
    }
    
    /**
     * Track the points earned for the given teams in the given game
     * If the event is not active, nothing happens.
     * @param teamIds The teams to track points for
     * @param points the points to add
     * @param gameType the game that the points came from
     */
    public void trackPointsTeams(Collection<String> teamIds, int points, GameType gameType) {
        if (state instanceof OffState) {
            return;
        }
        List<ScoreKeeper> iterationScoreKeepers = scoreKeepers.get(gameType);
        ScoreKeeper iteration = iterationScoreKeepers.getLast();
        for (String teamId : teamIds) {
            iteration.addPoints(teamId, points);
        }
    }
    
    /**
     * Track the points earned for the given participant in the given game. 
     * If the event is not active, nothing happens.
     * @param participantUUID The participant to track points for
     * @param points the points to add 
     * @param gameType the game that the points came from
     */
    public void trackPoints(UUID participantUUID, int points, GameType gameType) {
        if (state instanceof OffState) {
            return;
        }
        List<ScoreKeeper> iterationScoreKeepers = scoreKeepers.get(gameType);
        ScoreKeeper iteration = iterationScoreKeepers.getLast();
        iteration.addPoints(participantUUID, points);
    }
    
    /**
     * Track the points earned for the given participants in the given game. 
     * If the event is not active, nothing happens.
     * @param participants The participants to track points for
     * @param points the points to add 
     * @param gameType the game that the points came from
     */
    public void trackPointsParticipants(Collection<Participant> participants, int points, GameType gameType) {
        if (state instanceof OffState) {
            return;
        }
        List<ScoreKeeper> iterationScoreKeepers = scoreKeepers.get(gameType);
        ScoreKeeper iteration = iterationScoreKeepers.getLast();
        for (Participant participant : participants) {
            iteration.addPoints(participant.getUniqueId(), points);
        }
    }
    
    public void messageAllAdmins(Component message) {
        gameManager.messageAdmins(message);
    }
    
    public boolean allGamesHaveBeenPlayed() {
        return currentGameNumber >= maxGames + 1;
    }
}
