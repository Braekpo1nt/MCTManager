package org.braekpo1nt.mctmanager.games.event;

import lombok.Data;
import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.braekpo1nt.mctmanager.games.colossalcombat.ColossalCombatGame;
import org.braekpo1nt.mctmanager.games.event.config.EventConfig;
import org.braekpo1nt.mctmanager.games.event.config.EventConfigController;
import org.braekpo1nt.mctmanager.games.event.states.EventState;
import org.braekpo1nt.mctmanager.games.event.states.OffState;
import org.braekpo1nt.mctmanager.games.event.states.PodiumState;
import org.braekpo1nt.mctmanager.games.game.enums.GameType;
import org.braekpo1nt.mctmanager.games.voting.VoteManager;
import org.braekpo1nt.mctmanager.ui.sidebar.KeyLine;
import org.braekpo1nt.mctmanager.ui.sidebar.Sidebar;
import org.braekpo1nt.mctmanager.ui.timer.TimerManager;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

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
    private String winningTeam;
    
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
        // TODO: make this a state-dependant method
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
        state.stopEvent(sender);
    }
    
    public void undoGame(@NotNull CommandSender sender, @NotNull GameType gameType, int iterationIndex) {
        state.undoGame(sender, gameType, iterationIndex);
    }
    
    @EventHandler
    public void onPlayerDamage(EntityDamageEvent event) {
        state.onPlayerDamage(event);
    }
    
    @EventHandler
    public void onClickInventory(InventoryClickEvent event) {
        state.onClickInventory(event);
    }
    
    @EventHandler
    public void onDropItem(PlayerDropItemEvent event) {
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
    
    public boolean eventIsActive() {
        return !(state instanceof OffState);
    }
    
    public void cancelAllTasks() {
        voteManager.cancelVote();
        timerManager.cancel();
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
}
