package org.braekpo1nt.mctmanager.games;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.games.enums.MCTGames;
import org.braekpo1nt.mctmanager.utils.ColorMap;
import org.braekpo1nt.mctmanager.games.capturetheflag.CaptureTheFlagGame;
import org.braekpo1nt.mctmanager.games.finalgame.FinalGame;
import org.braekpo1nt.mctmanager.games.footrace.FootRaceGame;
import org.braekpo1nt.mctmanager.games.gamestate.GameStateStorageUtil;
import org.braekpo1nt.mctmanager.games.interfaces.MCTGame;
import org.braekpo1nt.mctmanager.games.mecha.MechaGame;
import org.braekpo1nt.mctmanager.games.parkourpathway.ParkourPathwayGame;
import org.braekpo1nt.mctmanager.games.spleef.SpleefGame;
import org.braekpo1nt.mctmanager.games.voting.VoteManager;
import org.braekpo1nt.mctmanager.hub.HubManager;
import org.braekpo1nt.mctmanager.ui.FastBoardManager;
import org.bukkit.*;
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
    private final SpleefGame spleefGame;
    private final ParkourPathwayGame parkourPathwayGame;
    private final FinalGame finalGame;
    private final CaptureTheFlagGame captureTheFlagGame;
    private final HubManager hubManager;
    private FastBoardManager fastBoardManager;
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
    private String finalGameTeamA;
    private String finalGameTeamB;

    public GameManager(Main plugin, Scoreboard mctScoreboard) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        this.mctScoreboard = mctScoreboard;
        this.gameStateStorageUtil = new GameStateStorageUtil(plugin);
        this.voteManager = new VoteManager(this, plugin);
        this.footRaceGame = new FootRaceGame(plugin, this);
        this.mechaGame = new MechaGame(plugin, this);
        this.spleefGame = new SpleefGame(plugin, this);
        this.parkourPathwayGame = new ParkourPathwayGame(plugin, this);
        this.captureTheFlagGame = new CaptureTheFlagGame(plugin, this);
        this.finalGame = new FinalGame(plugin, this);
        this.fastBoardManager = new FastBoardManager(gameStateStorageUtil);
        this.hubManager = new HubManager(plugin, mctScoreboard, fastBoardManager, this);
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
    
    public void startVote(@NotNull CommandSender sender, List<MCTGames> votingPool) {
        List<Player> onlineParticipants = this.getOnlineParticipants();
        if (onlineParticipants.isEmpty()) {
            sender.sendMessage(Component.text("There are no online participants. You can add participants using:\n")
                    .append(Component.text("/mct team join <team> <member>")
                            .decorate(TextDecoration.BOLD)
                            .clickEvent(ClickEvent.suggestCommand("/mct team join "))));
            return;
        }
        voteManager.startVote(onlineParticipants, votingPool);
    }
    
    /**
     * Cancel the vote if a vote is in progress
     */
    public void cancelVote() {
        voteManager.cancelVote();
    }
    
    /**
     * Cancel the return to hub if it's in progress
     */
    public void cancelReturnToHub() {
        hubManager.cancelReturnToHub();
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
            case SPLEEF -> {
                spleefGame.start(onlineParticipants);
                activeGame = spleefGame;
            }
            case PARKOUR_PATHWAY -> {
                parkourPathwayGame.start(onlineParticipants);
                activeGame = parkourPathwayGame;
            }
            case FINAL_GAME -> {
                if (finalGameTeamA == null || finalGameTeamB == null) {
                    sender.sendMessage(Component.text("Final game teams not set. Use /mct game finalgame <teamA> <teamB>")
                            .color(NamedTextColor.RED));
                    return;
                }
                List<Player> finalGameParticipants = new ArrayList<>();
                List<Player> onlinePlayersOnTeamA = getOnlinePlayersOnTeam(finalGameTeamA);
                if (onlinePlayersOnTeamA.size() == 0) {
                    sender.sendMessage(Component.empty()
                            .append(Component.text("There are no players online from team "))
                            .append(Component.text(finalGameTeamA)));
                    return;
                }
                List<Player> onlinePlayersOnTeamB = getOnlinePlayersOnTeam(finalGameTeamB);
                if (onlinePlayersOnTeamB.size() == 0) {
                    sender.sendMessage(Component.empty()
                            .append(Component.text("There are no players online from team "))
                            .append(Component.text(finalGameTeamB)));
                    return;
                }
                finalGameParticipants.addAll(onlinePlayersOnTeamA);
                finalGameParticipants.addAll(onlinePlayersOnTeamB);
                
                List<Player> otherParticipants = new ArrayList<>();
                for (Player player : getOnlineParticipants()) {
                    if (!finalGameParticipants.contains(player)) {
                        otherParticipants.add(player);
                    }
                }
                finalGame.teleportViewers(otherParticipants);
                
                finalGame.start(finalGameParticipants);
                activeGame = finalGame;
            }
        }
    }
    
    /**
     * Checks if a game is currently running
     * @return True if a game is running, false if not
     */
    public boolean gameIsRunning() {
        return activeGame != null;
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
    
    public void finalGameIsOver(String winningTeamName) {
        Bukkit.getLogger().info("Final game is over");
        new BukkitRunnable() {
            @Override
            public void run() {
                activeGame = null;
                String winningTeamDisplayName = gameStateStorageUtil.getTeamDisplayName(winningTeamName);
                List<Player> winningTeamParticipants = getOnlinePlayersOnTeam(winningTeamName);
                String colorString = gameStateStorageUtil.getTeamColorString(winningTeamName);
                ChatColor chatColor = ColorMap.getChatColor(colorString);
                List<Player> otherParticipants = new ArrayList<>();
                for (Player player : getOnlineParticipants()) {
                    if (!winningTeamParticipants.contains(player)) {
                        otherParticipants.add(player);
                    }
                }
                hubManager.pedestalTeleport(winningTeamParticipants, winningTeamDisplayName, chatColor, otherParticipants);
            }
        }.runTaskLater(plugin, 5*20);
    }
    
    /**
     * gets a list of the online participants
     * @return a list of all online participants
     */
    public List<Player> getOnlineParticipants() {
        List<Player> onlineParticipants = new ArrayList<>();
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (gameStateStorageUtil.containsPlayer(player.getUniqueId())) {
                onlineParticipants.add(player);
            }
        }
        return onlineParticipants;
    }
    
    //====================================================
    // GameStateStorageUtil accessors and helpers
    //====================================================
    
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
        if (team != null){
            team.unregister();
        }
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
     * Gets a list of all unique team names which the given participants belong to.
     * @param participants The list of participants to get the team names of
     * @return A list of all unique team names which the given participants belong to.
     */
    public List<String> getTeamNames(List<Player> participants) {
        List<String> teamNames = new ArrayList<>();
        for (Player player : participants) {
            String teamName = getTeamName(player.getUniqueId());
            if (!teamNames.contains(teamName)){
                teamNames.add(teamName);
            }
        }
        return teamNames;
    }
    
    /**
     * Checks if the team exists in the game state
     * @param teamName The team to look for
     * @return true if the team with the given teamName exists, false otherwise.
     */
    public boolean hasTeam(String teamName) {
        return gameStateStorageUtil.containsTeam(teamName);
    }
    
    /**
     * Checs if the player exists in the game state
     * @param playerUniqueId The UUID of the player to check for
     * @return true if the UUID is in the game state, false otherwise
     */
    public boolean isParticipant(UUID playerUniqueId) {
        return gameStateStorageUtil.containsPlayer(playerUniqueId);
    }
    
    /**
     * Joins the player with the given UUID to the team with the given teamName, and adds them
     * to the game state.
     * @param player The player to join to the given team
     * @param teamName The internal teamName of the team to join the player to. 
     *                 This method assumes the team exists, and will throw a 
     *                 null pointer exception if it doesn't.
     */
    public void joinPlayerToTeam(Player player, String teamName) throws IOException {
        UUID playerUniqueId = player.getUniqueId();
        if (gameStateStorageUtil.containsPlayer(playerUniqueId)) {
            movePlayerToTeam(playerUniqueId, teamName);
            player.sendMessage(Component.text("You've been moved to ")
                    .append(getFormattedTeamDisplayName(teamName)));
            return;
        }
        addNewPlayer(playerUniqueId, teamName);
        player.sendMessage(Component.text("You've been joined to ")
                .append(getFormattedTeamDisplayName(teamName)));
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
    
    /**
     * Gets the online players who are on the given team. 
     * @param teamName The internal name of the team
     * @return A list of all online players on that team, 
     * or empty list if there are no players on that team or the team doesn't exist.
     */
    public List<Player> getOnlinePlayersOnTeam(String teamName) {
        List<UUID> playerUniqueIds = gameStateStorageUtil.getPlayerUniqueIdsOnTeam(teamName);
        List<Player> onlinePlayersOnTeam = new ArrayList<>();
        for (UUID playerUniqueId : playerUniqueIds) {
            Player player = Bukkit.getPlayer(playerUniqueId);
            if (player != null && player.isOnline()) {
                onlinePlayersOnTeam.add(player);
            }
        }
        return onlinePlayersOnTeam;
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
            String teamName = gameStateStorageUtil.getPlayerTeamName(playerUniqueId);
            addScore(playerUniqueId, points);
            addScore(teamName, points);
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
    
    /**
     * Adds the given points to the given team, and announces to all online members of that team how many points the team earned.
     * @param teamName The team to add points to
     * @param points The points to add
     */
    public void awardPointsToTeam(String teamName, int points) {
        if (!gameStateStorageUtil.containsTeam(teamName)) {
            return;
        }
        try {
            addScore(teamName, points);
            Component displayName = getFormattedTeamDisplayName(teamName);
            List<Player> playersOnTeam = getOnlinePlayersOnTeam(teamName);
            for (Player playerOnTeam : playersOnTeam) {
                playerOnTeam.sendMessage(Component.text("+")
                        .append(Component.text(points))
                        .append(Component.text(" points for "))
                        .append(displayName)
                        .decorate(TextDecoration.BOLD)
                        .color(NamedTextColor.GOLD));
            }
        } catch (IOException e) {
            Bukkit.getLogger().severe("Error while adding points to team. See log for error message.");
            throw new RuntimeException(e);
        }
    }
    
    public Color getTeamColor(UUID playerUniqueId) {
        return gameStateStorageUtil.getTeamColor(playerUniqueId);
    }
    
    public NamedTextColor getTeamNamedTextColor(String teamName) {
        return gameStateStorageUtil.getTeamNamedTextColor(teamName);
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
    
    public String getTeamDisplayName(String teamName) {
        return gameStateStorageUtil.getTeamDisplayName(teamName);
    }
    
    /**
     * Returns the names of all online participants
     * @return A list of the names of all online participants. Empty list if none are online.
     */
    public List<String> getOnlineParticipantNames() {
        List<Player> onlinePlayers = getOnlineParticipants();
        List<String> names = new ArrayList<>();
        for (Player player : onlinePlayers) {
            names.add(player.getName());
        }
        return names;
    }
    
    
    /**
     * Gets all the names of the participants in the game state, regardless of 
     * whether they're offline or online. 
     * @return
     */
    public List<String> getAllParticipantNames() {
        List<OfflinePlayer> offlinePlayers = getOfflineParticipants();
        List<String> playerNames = new ArrayList<>();
        for (OfflinePlayer offlinePlayer : offlinePlayers) {
            String name = offlinePlayer.getName();
            playerNames.add(name);
        }
        return playerNames;
    }
    
    /**
     * Gets a list of all participants in the form of OfflinePlayers. This will
     * return all participants in the game state whether they are offline or online. 
     * @return A list of all OfflinePlayers in the game state. These players could
     * be offline or online, exist or not. The only guarantee is that their UUID is
     * in the game state. 
     */
    public List<OfflinePlayer> getOfflineParticipants() {
        List<UUID> uniqueIds = gameStateStorageUtil.getPlayerUniqueIds();
        List<OfflinePlayer> offlinePlayers = new ArrayList<>();
        for (UUID uniqueId : uniqueIds) {
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uniqueId);
            offlinePlayers.add(offlinePlayer);
        }
        return offlinePlayers;
    }
    
    /**
     * Adds the given score to the participant with the given UUID
     * @param participantUniqueId The UUID of the participant to add the score to
     * @param score The score to add. Could be positive or negative.
     * @throws IOException If there is a problem saving the game state.
     */
    public void addScore(UUID participantUniqueId, int score) throws IOException {
        gameStateStorageUtil.addScore(participantUniqueId, score);
    }
    
    /**
     * Adds the given score to the team with the given name
     * @param teamName The name of the team to add the score to
     * @param score The score to add. Could be positive or negative.
     * @throws IOException If there is a problem saving the game state.
     */
    public void addScore(String teamName, int score) throws IOException {
        gameStateStorageUtil.addScore(teamName, score);
    }
    
    /**
     * Sets the score of the participant with the given UUID to the given value
     * @param participantUniqueId The UUID of the participant to set the score to
     * @param score The score to set to. If the score is negative, the score will be set to 0.
     * @throws IOException If there is a problem saving the game state.
     */
    public void setScore(UUID participantUniqueId, int score) throws IOException {
        if (score < 0) {
            gameStateStorageUtil.setScore(participantUniqueId, 0);
            return;
        }
        gameStateStorageUtil.setScore(participantUniqueId, score);
    }
    
    /**
     * Sets the score of the team with the given name to the given value
     * @param teamName The UUID of the participant to set the score to
     * @param score The score to set to. If the score is negative, the score will be set to 0.
     * @throws IOException If there is a problem saving the game state.
     */
    public void setScore(String teamName, int score) throws IOException {
        if (score < 0) {
            gameStateStorageUtil.setScore(teamName, 0);
            return;
        }
        gameStateStorageUtil.setScore(teamName, score);
    }
    
    /**
     * Gets the score of the given team
     * @param teamName The team to get the score of
     * @return The score of the given team
     */
    public int getScore(String teamName) {
        return gameStateStorageUtil.getTeamScore(teamName);
    }
    
    /**
     * Gets the score of the participant with the given UUID
     * @param participantUniqueId The UUID of the participant to get the score of
     * @return The score of the participant with the given UUID
     */
    public int getScore(UUID participantUniqueId) {
        return gameStateStorageUtil.getPlayerScore(participantUniqueId);
    }
    
    public void setFinalGameTeams(String teamA, String teamB) {
        this.finalGameTeamA = teamA;
        this.finalGameTeamB = teamB;
    }
    
    public Material getTeamPowderColor(String teamName) {
        String colorString = gameStateStorageUtil.getTeamColorString(teamName);
        return ColorMap.getConcretePowderColor(colorString);
    }
    
    public ChatColor getTeamChatColor(String teamName) {
        String colorString = gameStateStorageUtil.getTeamColorString(teamName);
        return ColorMap.getChatColor(colorString);
    }
    
    public Material getTeamBannerColor(String teamName) {
        String colorString = gameStateStorageUtil.getTeamColorString(teamName);
        return ColorMap.getBannerColor(colorString);
    }
    
    public void setFastBoardManager(FastBoardManager fastBoardManager) {
        this.fastBoardManager = fastBoardManager;
        this.fastBoardManager.setGameStateStorageUtil(this.gameStateStorageUtil);
    }
    
    // Test methods
    public MCTGame getActiveGame() {
        return activeGame;
    }
}
