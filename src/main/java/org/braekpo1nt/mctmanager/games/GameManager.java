package org.braekpo1nt.mctmanager.games;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.color.ColorMap;
import org.braekpo1nt.mctmanager.games.capturetheflag.CaptureTheFlagGame;
import org.braekpo1nt.mctmanager.games.footrace.FootRaceGame;
import org.braekpo1nt.mctmanager.games.gamestate.GameStateStorageUtil;
import org.braekpo1nt.mctmanager.games.interfaces.MCTGame;
import org.braekpo1nt.mctmanager.games.mecha.MechaGame;
import org.braekpo1nt.mctmanager.games.voting.VoteManager;
import org.braekpo1nt.mctmanager.hub.HubManager;
import org.braekpo1nt.mctmanager.ui.FastBoardManager;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.*;

/**
 * Responsible for overall game management. 
 * Creating new game instances, starting/stopping games, and handling game events.
 */
public class GameManager implements Listener {
    
    private MCTGame activeGame = null;
    private final FootRaceGame footRaceGame;
    private final MechaGame mechaGame;
    private final CaptureTheFlagGame captureTheFlagGame;
    private final HubManager hubManager;
    private final FastBoardManager fastBoardManager;
    private final GameStateStorageUtil gameStateStorageUtil;
    /**
     * Scoreboard for holding the teams. This private scoreboard can't be
     * modified using the normal /team command, and thus can't be unsynced
     * with the game state.
     */
    private final Scoreboard mctScoreboard;
    private final Main plugin;
    private boolean shouldTeleportToHub = true;
    private int fastBoardUpdaterTaskId;
    private final List<UUID> participantsWhoLeftMidGame = new ArrayList<>();
    private final VoteManager voteManager;
    
    public GameManager(Main plugin, Scoreboard mctScoreboard, HubManager hubManager) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        this.mctScoreboard = mctScoreboard;
        this.gameStateStorageUtil = new GameStateStorageUtil(plugin);
        this.voteManager = new VoteManager(this, plugin);
        this.footRaceGame = new FootRaceGame(plugin, this);
        this.mechaGame = new MechaGame(plugin, this);
        this.captureTheFlagGame = new CaptureTheFlagGame(plugin, this);
        this.hubManager = hubManager;
        this.fastBoardManager = new FastBoardManager(gameStateStorageUtil);
        kickOffFastBoardManager();
    }
    
    private void kickOffFastBoardManager() {
        this.fastBoardUpdaterTaskId = new BukkitRunnable() {
            @Override
            public void run() {
                fastBoardManager.updateMainBoards();
            }
        }.runTaskTimer(plugin, 0L, 20L).getTaskId();
    }
    
    public void cancelFastBoardManager() {
        Bukkit.getScheduler().cancelTask(this.fastBoardUpdaterTaskId);
        fastBoardManager.removeAllBoards();
    }
    
    @EventHandler
    public void playerQuitEvent(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (!isParticipant(player.getUniqueId())) {
            return;
        }
        onParticipantLeave(player);
    }
    
    /**
     * Handles when a participant leaves the event.
     * Should be called when the player disconnects from the server 
     * (see {@link GameManager#playerQuitEvent(PlayerQuitEvent)}),
     * or when they are removed from the participants list
     * (see {@link GameManager#leavePlayer(OfflinePlayer)})
     * @param participant The participant who left the event
     */
    private void onParticipantLeave(Player participant) {
        fastBoardManager.removeBoard(participant.getUniqueId());
        if (gameIsRunning()) {
            activeGame.onParticipantQuit(participant);
            participantsWhoLeftMidGame.add(participant.getUniqueId());
        }
    }
    
    @EventHandler
    public void playerJoinEvent(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (!isParticipant(player.getUniqueId())) {
            return;
        }
        onParticipantJoin(player);
    }
    
    /**
     * Handles when a participant joins the event. 
     * Should be called when an existing participant joins the server
     * (see {@link GameManager#playerJoinEvent(PlayerJoinEvent)})
     * or when a new participant is online and added to the participants list
     * (see {@link GameManager#addNewPlayer(UUID, String)})
     * @param participant
     */
    private void onParticipantJoin(Player participant) {
        fastBoardManager.updateMainBoards();
        if (!gameIsRunning()) {
            if (participantsWhoLeftMidGame.contains(participant.getUniqueId())) {
                participantsWhoLeftMidGame.remove(participant.getUniqueId());
                hubManager.returnParticipantToHub(participant);
            }
            return;
        }
        activeGame.onParticipantJoin(participant);
    }
    
    public Scoreboard getMctScoreboard() {
        return mctScoreboard;
    }
    
    public FastBoardManager getFastBoardManager() {
        return fastBoardManager;
    }
    
    public void loadGameState() throws IOException {
        gameStateStorageUtil.loadGameState();
        gameStateStorageUtil.setupScoreboard(mctScoreboard);
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.setScoreboard(mctScoreboard);
        }
    }
    
    public void saveGameState() throws IOException, IllegalStateException {
        gameStateStorageUtil.saveGameState();
    }
    
    public void startVote(@NotNull CommandSender sender) {
        List<Player> onlineParticipants = this.getOnlineParticipants();
        if (onlineParticipants.isEmpty()) {
            sender.sendMessage(Component.text("There are no online participants. You can add participants using:\n")
                    .append(Component.text("/mct team join <team> <member>")
                            .decorate(TextDecoration.BOLD)
                            .clickEvent(ClickEvent.suggestCommand("/mct team join "))));
            return;
        }
        voteManager.startVote(onlineParticipants);
    }
    
    /**
     * Cancel the vote if a vote is in progress
     */
    public void cancelVote() {
        voteManager.cancelVote();
    }
    
    
    public void startGame(MCTGames mctGame, @NotNull CommandSender sender) {
        
        if (activeGame != null) {
            sender.sendMessage("There is already a game running. You must stop the game before you start a new one.");
            return;
        }
        
        List<Player> onlineParticipants = this.getOnlineParticipants();
        if (onlineParticipants.isEmpty()) {
            sender.sendMessage(Component.text("There are no online participants. You can add participants using:\n")
                    .append(Component.text("/mct team join <team> <member>")
                            .decorate(TextDecoration.BOLD)
                            .clickEvent(ClickEvent.suggestCommand("/mct team join "))));
            return;
        }
        
        List<String> onlineTeamNames = this.getTeamNames(onlineParticipants);
        
        switch (mctGame) {
            case FOOT_RACE -> {
                footRaceGame.start(onlineParticipants);
                activeGame = footRaceGame;
            }
            case MECHA -> {
                if (onlineTeamNames.size() < 2) {
                    sender.sendMessage(Component.text("MECHA doesn't end correctly unless there are 2 or more teams online. use ")
                            .append(Component.text("/mct game stop")
                                    .clickEvent(ClickEvent.suggestCommand("/mct game stop"))
                                    .decorate(TextDecoration.BOLD))
                            .append(Component.text(" to stop the game."))
                            .color(NamedTextColor.RED));
                }
                mechaGame.start(onlineParticipants);
                activeGame = mechaGame;
            }
            case CAPTURE_THE_FLAG -> {
                if (onlineTeamNames.size() < 2 || 8 < onlineTeamNames.size()) {
                    sender.sendMessage(Component.text("Capture the Flag needs at least 2 and at most 8 teams online to play."));
                    return;
                }
                captureTheFlagGame.start(onlineParticipants);
                activeGame = captureTheFlagGame;
            }
            default -> {
            }
        }
    }
    
    /**
     * gets a list of the online participants
     * @return a list of all online participants
     */
    private List<Player> getOnlineParticipants() {
        List<Player> onlineParticipants = new ArrayList<>();
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (gameStateStorageUtil.containsPlayer(player.getUniqueId())) {
                onlineParticipants.add(player);
            }
        }
        return onlineParticipants;
    }
    
    /**
     * If a game is currently going on, manually stops the game.
     * @throws NullPointerException if no game is currently running. 
     * Check if a game is running with isGameRunning()
     */
    public void manuallyStopGame(boolean shouldTeleportToHub) {
        this.shouldTeleportToHub = shouldTeleportToHub;
        activeGame.stop();
        activeGame = null;
    }
    
    /**
     * Checks if a game is currently running
     * @return True if a game is running, false if not
     */
    public boolean gameIsRunning() {
        return activeGame != null;
    }
    
    /**
     * Meant to be called by the active game when the game is over.
     */
    public void gameIsOver() {
        activeGame = null;
        if (!shouldTeleportToHub) {
            shouldTeleportToHub = true;
            return;
        }
        hubManager.returnParticipantsToHubWithDelay(getOnlineParticipants());
    }
    
    /**
     * Remove the given team from the game
     * @param teamName The internal name of the team to remove
     * @return True if the team was successfully removed, false if the team did not exist
     * @throws IOException If there is a problem saving the game state while removing the team
     */
    public boolean removeTeam(String teamName) throws IOException {
        if (!gameStateStorageUtil.containsTeam(teamName)) {
            return false;
        }
        this.leavePlayersOnTeam(teamName);
        gameStateStorageUtil.removeTeam(teamName);
        Team team = mctScoreboard.getTeam(teamName);
        team.unregister();
        return true;
    }
    
    /**
     * Add a team to the game.
     * @param teamName The internal name of the team.
     * @param teamDisplayName The display name of the team.
     * @return True if the team was successfully created. False if the team already exists.
     * @throws IOException If there was a problem saving the game state with the new team.
     */
    public boolean addTeam(String teamName, String teamDisplayName, String colorString) throws IOException {
        if (gameStateStorageUtil.containsTeam(teamName)) {
            return false;
        }
        gameStateStorageUtil.addTeam(teamName, teamDisplayName, colorString);
        Team newTeam = mctScoreboard.registerNewTeam(teamName);
        newTeam.displayName(Component.text(teamDisplayName));
        NamedTextColor color = ColorMap.getNamedTextColor(colorString);
        newTeam.color(color);
        return true;
    }
    
    /**
     * A list of all the teams in the game
     * @return A list containing the internal names of all the teams in the game. 
     * Empty list if there are no teams
     */
    public Set<String> getTeamNames() {
        return gameStateStorageUtil.getTeamNames();
    }
    
    /**
     * Gets a list of all the team names of the players
     * @param players The list of players to get the team names of
     * @return A list of all unique team names which the players belong to.
     */
    public List<String> getTeamNames(List<Player> players) {
        List<String> teamNames = new ArrayList<>();
        for (Player player : players) {
            String teamName = getTeamName(player.getUniqueId());
            if (!teamNames.contains(teamName)){
                teamNames.add(teamName);
            }
        }
        return teamNames;
    }
    
    /**
     * Checks if the team exists in the game
     * @param teamName The team to look for
     * @return true if the team with the given teamName exists, false otherwise.
     */
    public boolean hasTeam(String teamName) {
        return gameStateStorageUtil.containsTeam(teamName);
    }
    
    /**
     * Joins the player with the given UUID to the team with the given teamName, and adds them
     * to the game state.
     * @param playerUniqueId The UUID of the player to join to the given team
     * @param teamName The internal teamName of the team to join the player to. 
     *                 This method assumes the team exists, and will throw a 
     *                 null pointer exception if it doesn't.
     */
    public void joinPlayerToTeam(UUID playerUniqueId, String teamName) throws IOException {
        if (gameStateStorageUtil.containsPlayer(playerUniqueId)) {
            movePlayerToTeam(playerUniqueId, teamName);
            return;
        }
        addNewPlayer(playerUniqueId, teamName);
    }
    
    private void movePlayerToTeam(UUID playerUniqueId, String newTeamName) {
        String oldTeamName = gameStateStorageUtil.getPlayerTeamName(playerUniqueId);
        gameStateStorageUtil.setPlayerTeamName(playerUniqueId, newTeamName);
    
        OfflinePlayer player = Bukkit.getOfflinePlayer(playerUniqueId);
        Team oldTeam = mctScoreboard.getTeam(oldTeamName);
        oldTeam.removePlayer(player);
        Team newTeam = mctScoreboard.getTeam(newTeamName);
        newTeam.addPlayer(player);
    }
    
    public List<String> getPlayerNamesOnTeam(String teamName) {
        List<UUID> playerUniqueIds = gameStateStorageUtil.getPlayerUniqueIdsOnTeam(teamName);
        List<String> playersNamesOnTeam = new ArrayList<>();
        for (UUID playerUniqueId : playerUniqueIds) {
            OfflinePlayer playerOnTeam = Bukkit.getOfflinePlayer(playerUniqueId);
            playersNamesOnTeam.add(playerOnTeam.getName());
        }
        return playersNamesOnTeam;
    }
    
    public boolean isParticipant(UUID playerUniqueId) {
        return gameStateStorageUtil.containsPlayer(playerUniqueId);
    }
    
    /**
     * Adds the new player to the game state and joins them the given team. 
     * If a game is running, and the player is online, joins the player to that game.  
     * @param playerUniqueId The UUID of the player to add
     * @param teamName The name of the team to join the new player to
     * @throws IOException If there is an issue saving the game state when adding the player
     */
    private void addNewPlayer(UUID playerUniqueId, String teamName) throws IOException {
        gameStateStorageUtil.addNewPlayer(playerUniqueId, teamName);
        Team team = mctScoreboard.getTeam(teamName);
        OfflinePlayer newPlayer = Bukkit.getOfflinePlayer(playerUniqueId);
        team.addPlayer(newPlayer);
        if (newPlayer.isOnline()) {
            Player onlineNewPlayer = newPlayer.getPlayer();
            onParticipantJoin(onlineNewPlayer);
        }
    }
    
    /**
     * Leaves the player from the team and removes them from the game state.
     * If a game is running, and the player is online, removes that player from the game as well. 
     * @param offlinePlayer The player to remove from the team
     * @throws IOException If there is an issue saving the game state when removing the player
     */
    public void leavePlayer(OfflinePlayer offlinePlayer) throws IOException {
        if (offlinePlayer.isOnline()) {
            Player onlinePlayer = offlinePlayer.getPlayer();
            onParticipantLeave(onlinePlayer);
        }
        UUID playerUniqueId = offlinePlayer.getUniqueId();
        String teamName = gameStateStorageUtil.getPlayerTeamName(playerUniqueId);
        gameStateStorageUtil.leavePlayer(playerUniqueId);
        Team team = mctScoreboard.getTeam(teamName);
        team.removePlayer(offlinePlayer);
        fastBoardManager.removeBoard(playerUniqueId);
    }
    
    private void leavePlayersOnTeam(String teamName) throws IOException {
        List<UUID> playerUniqueIds = gameStateStorageUtil.getPlayerUniqueIdsOnTeam(teamName);
        for (UUID playerUniqueId : playerUniqueIds) {
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerUniqueId);
            leavePlayer(offlinePlayer);
        }
    }
    
    public String getTeamName(UUID playerUniqueId) {
        return gameStateStorageUtil.getPlayerTeamName(playerUniqueId);
    }
    
    /**
     * Awards points to the player and their team. If the player does not exist, nothing happens.
     * @param player The player to award points to
     * @param points The points to award to the player
     */
    public void awardPointsToPlayer(Player player, int points) {
        UUID playerUniqueId = player.getUniqueId();
        if (!gameStateStorageUtil.containsPlayer(playerUniqueId)) {
            return;
        }
        try {
            gameStateStorageUtil.addPointsToPlayer(playerUniqueId, points);
        } catch (IOException e) {
            player.sendMessage(
                    Component.text("Critical error occurred. Please notify an admin to check the logs.")
                            .color(NamedTextColor.RED)
                            .decorate(TextDecoration.BOLD));
            Bukkit.getLogger().severe("Error while adding points to player. See log for error message.");
            throw new RuntimeException(e);
        }
        player.sendMessage(Component.text("+")
                .append(Component.text(points))
                .append(Component.text(" points"))
                .decorate(TextDecoration.BOLD)
                .color(NamedTextColor.GOLD));
    }
    
    public Color getTeamColor(UUID playerUniqueId) {
        return gameStateStorageUtil.getTeamColor(playerUniqueId);
    }
    
    /**
     * Gets the team's display name as a Component with the team's text color
     * and in bold
     * @param teamName The internal name of the team
     * @return A Component with the formatted team dislay name
     */
    public Component getFormattedTeamDisplayName(String teamName) {
        String displayName = gameStateStorageUtil.getTeamDisplayName(teamName);
        NamedTextColor teamColor = gameStateStorageUtil.getTeamNamedTextColor(teamName);
        return Component.text(displayName).color(teamColor).decorate(TextDecoration.BOLD);
    }
}
