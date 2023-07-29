package org.braekpo1nt.mctmanager.games;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.games.clockwork.ClockworkGame;
import org.braekpo1nt.mctmanager.games.colossalcolosseum.ColossalColosseumGame;
import org.braekpo1nt.mctmanager.games.enums.MCTGames;
import org.braekpo1nt.mctmanager.games.utils.GameManagerUtils;
import org.braekpo1nt.mctmanager.ui.TimeStringUtils;
import org.braekpo1nt.mctmanager.utils.ColorMap;
import org.braekpo1nt.mctmanager.games.capturetheflag.CaptureTheFlagGame;
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
    
    public static final String ADMIN_TEAM = "_Admins";
    private MCTGame activeGame = null;
    private final FootRaceGame footRaceGame;
    private final MechaGame mechaGame;
    private final SpleefGame spleefGame;
    private final ParkourPathwayGame parkourPathwayGame;
    private final ColossalColosseumGame colossalColosseumGame;
    private final CaptureTheFlagGame captureTheFlagGame;
    private final ClockworkGame clockworkGame;
    private final HubManager hubManager;
    private FastBoardManager fastBoardManager;
    private GameStateStorageUtil gameStateStorageUtil;
    /**
     * Scoreboard for holding the teams. This private scoreboard can't be
     * modified using the normal /team command, and thus can't be unsynced
     * with the game state.
     */
    private final Scoreboard mctScoreboard;
    private final Main plugin;
    private boolean shouldTeleportToHub = true;
    private int fastBoardUpdaterTaskId;
    private int finalGameEndTaskId;
    private final List<UUID> participantsWhoLeftMidGame = new ArrayList<>();
    private final VoteManager voteManager;
    private String firstPlaceTeamName;
    private String secondPlaceTeamName;
    private boolean eventActive = false;
    private boolean eventPaused = false;
    private CommandSender eventMaster;
    private int currentGameNumber = 0;
    private int maxGames = 6;
    /**
     * Contains the list of online participants. Updated when participants are added/removed or quit/join
     */
    private final List<Player> onlineParticipants = new ArrayList<>();
    private final List<Player> onlineAdmins = new ArrayList<>();
    private int startGameWithDelayTaskId;
    private boolean startingGameWithDelay;
    
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
        this.clockworkGame = new ClockworkGame(plugin, this);
        this.colossalColosseumGame = new ColossalColosseumGame(plugin, this);
        this.fastBoardManager = new FastBoardManager(gameStateStorageUtil);
        this.hubManager = new HubManager(plugin, mctScoreboard, this);
        this.eventMaster = Bukkit.getConsoleSender();
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
        Bukkit.getScheduler().cancelTask(this.finalGameEndTaskId);
        fastBoardManager.removeAllBoards();
    }
    
    @EventHandler
    public void playerQuitEvent(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (isAdmin(player.getUniqueId())) {
            onAdminQuit(player);
            return;
        }
        if (isParticipant(player.getUniqueId())) {
            onParticipantQuit(player);
        }
    }
    
    private void onAdminQuit(@NotNull Player admin) {
        onlineAdmins.remove(admin);
        fastBoardManager.removeBoard(admin.getUniqueId());
    }
    
    /**
     * Handles when a participant leaves the event.
     * Should be called when a participant disconnects (quits/leaves) from the server 
     * (see {@link GameManager#playerQuitEvent(PlayerQuitEvent)}),
     * or when they are removed from the participants list
     * (see {@link GameManager#leavePlayer(OfflinePlayer)})
     * @param participant The participant who left the event
     */
    private void onParticipantQuit(@NotNull Player participant) {
        onlineParticipants.remove(participant);
        fastBoardManager.removeBoard(participant.getUniqueId());
        if (gameIsRunning()) {
            activeGame.onParticipantQuit(participant);
            participantsWhoLeftMidGame.add(participant.getUniqueId());
            return;
        }
        hubManager.onParticipantQuit(participant);
    }
    
    @EventHandler
    public void playerJoinEvent(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (isAdmin(player.getUniqueId())) {
            onAdminJoin(player);
            return;
        }
        if (isParticipant(player.getUniqueId())) {
            onParticipantJoin(player);
        }
    }
    
    private void onAdminJoin(@NotNull Player admin) {
        onlineAdmins.add(admin);
        fastBoardManager.updateMainBoards();
    }
    
    /**
     * Handles when a participant joins the event. 
     * Should be called when an existing participant joins the server
     * (see {@link GameManager#playerJoinEvent(PlayerJoinEvent)})
     * or when an online player is added to the participants list
     * (see {@link GameManager#addNewPlayer(UUID, String)})
     * @param participant the participant
     */
    private void onParticipantJoin(@NotNull Player participant) {
        onlineParticipants.add(participant);
        fastBoardManager.updateMainBoards();
        if (gameIsRunning()) {
            activeGame.onParticipantJoin(participant);
            return;
        }
        if (participantsWhoLeftMidGame.contains(participant.getUniqueId())) {
            participantsWhoLeftMidGame.remove(participant.getUniqueId());
            hubManager.returnParticipantToHub(participant);
            return;
        }
        hubManager.onParticipantJoin(participant);
    }
    
    public Scoreboard getMctScoreboard() {
        return mctScoreboard;
    }
    
    public FastBoardManager getFastBoardManager() {
        return fastBoardManager;
    }
    
    public boolean loadGameState() {
        try {
            gameStateStorageUtil.loadGameState();
        } catch (IOException e) {
            reportGameStateIOException("loading game state", e);
            return false;
        }
        gameStateStorageUtil.setupScoreboard(mctScoreboard);
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.setScoreboard(mctScoreboard);
        }
        onlineParticipants.clear();
        onlineAdmins.clear();
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (gameStateStorageUtil.isAdmin(player.getUniqueId())) {
                onlineAdmins.add(player);
            }
            if (gameStateStorageUtil.containsPlayer(player.getUniqueId())) {
                onlineParticipants.add(player);
                hubManager.onParticipantJoin(player);
            }
        }
        return true;
    }
    
    public void saveGameState() {
        try {
            gameStateStorageUtil.saveGameState();
        } catch (IOException e) {
            reportGameStateIOException("adding score to player", e);
        }
    }
    
    /**
     * For the "/mct game vote" command. Starts the vote with the specified voting pool.
     * @param sender The sender of the command
     * @param votingPool The games to vote between
     */
    public void manuallyStartVote(@NotNull CommandSender sender, List<MCTGames> votingPool) {
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
     * Starts the voting phase for the event
     */
    public void startVote() {
        if (currentGameNumber > maxGames) {
            eventMaster.sendMessage(Component.text("All games have been played. Initiating final game with top two teams."));
            kickOffFinalGame();
            return;
        }
        
        List<MCTGames> playedGames = gameStateStorageUtil.getPlayedGames();
        
        List<MCTGames> votingPool = new ArrayList<>();
        votingPool.add(MCTGames.FOOT_RACE);
        votingPool.add(MCTGames.MECHA);
        votingPool.add(MCTGames.CAPTURE_THE_FLAG);
        votingPool.add(MCTGames.SPLEEF);
        votingPool.add(MCTGames.PARKOUR_PATHWAY);
        votingPool.add(MCTGames.CLOCKWORK);
        votingPool.removeAll(playedGames);
        
        if (votingPool.isEmpty()) {
            eventMaster.sendMessage(Component.text("No more games to play. Initiating final game with top two teams."));
            kickOffFinalGame();
            return;
        }
        
        voteManager.startVote(onlineParticipants, votingPool);
    }
    
    private void updateStartGameDelayFastBoard(Player voter, String gameTitle, String timeString) {
        fastBoardManager.updateLines(
                voter.getUniqueId(),
                "",
                gameTitle,
                timeString
        );
    }
    
    private void kickOffFinalGame() {
        List<String> allTeams = getTeamNames(onlineParticipants);
        if (allTeams.size() < 2) {
            eventMaster.sendMessage(Component.empty()
                    .append(Component.text("There are fewer than two teams online. Use "))
                    .append(Component.text("/mct game finalgame <first> <second>")
                            .clickEvent(ClickEvent.suggestCommand("/mct game finalgame "))
                            .decorate(TextDecoration.BOLD))
                    .append(Component.text(" to start the final game with the two chosen teams.")));
            return;
        }
        Map<String, Integer> teamScores = new HashMap<>();
        for (String teamName : allTeams) {
            int score = getScore(teamName);
            teamScores.put(teamName, score);
        }
        String[] firstPlaces = GameManagerUtils.calculateFirstPlace(teamScores);
        if (firstPlaces.length == 2) {
            String firstPlace = firstPlaces[0];
            String secondPlace = firstPlaces[1];
            setFinalGameTeams(firstPlace, secondPlace);
            startGame(MCTGames.COLOSSAL_COLOSSEUM);
            return;
        }
        if (firstPlaces.length > 2) {
            eventMaster.sendMessage(Component.text("There are more than 2 teams tied for first place. A tie breaker is needed. Use ")
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
            eventMaster.sendMessage(Component.text("There is a tie second place. A tie breaker is needed. Use ")
                    .append(Component.text("/mct game finalgame <first> <second>")
                            .clickEvent(ClickEvent.suggestCommand("/mct game finalgame "))
                            .decorate(TextDecoration.BOLD))
                    .append(Component.text(" to start the final game with the two chosen teams."))
                    .color(NamedTextColor.RED));
            return;
        }
        String secondPlace = secondPlaces[0];
        setFinalGameTeams(firstPlace, secondPlace);
        startGame(MCTGames.COLOSSAL_COLOSSEUM);
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
    public void cancelAllTasks() {
        Bukkit.getScheduler().cancelTask(this.finalGameEndTaskId);
        Bukkit.getScheduler().cancelTask(this.fastBoardUpdaterTaskId);
        cancelAllEventTasks();
        hubManager.cancelAllTasks();
    }
    
    private void cancelAllEventTasks() {
        Bukkit.getScheduler().cancelTask(this.startGameWithDelayTaskId);
    }
    
    public void startEvent(CommandSender eventMaster, int maxGames) {
        if (eventActive) {
            eventMaster.sendMessage(Component.text("An event is already running.")
                    .color(NamedTextColor.RED));
            return;
        }
        if (activeGame != null) {
            eventMaster.sendMessage(Component.text("Can't start an event while a game is running.")
                    .color(NamedTextColor.RED));
            return;
        }
        this.eventMaster = eventMaster;
        currentGameNumber = 1;
        this.maxGames = maxGames;
        eventActive = true;
        try {
            gameStateStorageUtil.clearPlayedGames();
        } catch (IOException e) {
            eventMaster.sendMessage(Component.text(
                            "Error clearing played games. See log for error message.")
                    .color(NamedTextColor.RED));
            Bukkit.getLogger().severe("Error clearing played games. See log for error message.");
            throw new RuntimeException(e);
        }
        eventMaster.sendMessage(Component.text("Starting event. On game ")
                .append(Component.text(currentGameNumber))
                .append(Component.text("/"))
                .append(Component.text(maxGames))
                .append(Component.text(".")));
        hubManager.returnParticipantsToHub(onlineParticipants);
    }
    
    public void stopEvent(CommandSender sender) {
        if (!eventActive) {
            sender.sendMessage(Component.text("There is no event running.")
                    .color(NamedTextColor.RED));
            return;
        }
        eventActive = false;
        Component message = Component.text("Ending event. ")
                .append(Component.text(currentGameNumber - 1))
                .append(Component.text("/"))
                .append(Component.text(maxGames))
                .append(Component.text(" games were played."));
        sender.sendMessage(message);
        if (!sender.equals(eventMaster)){
            eventMaster.sendMessage(message);
        }
        cancelAllEventTasks();
        hubManager.eventIsOver();
        currentGameNumber = 0;
        this.maxGames = 6;
    }
    
    public void pauseEvent(CommandSender sender) {
        if (!eventActive) {
            sender.sendMessage(Component.text("There is no event running.")
                    .color(NamedTextColor.RED));
            return;
        }
        if (activeGame != null || startingGameWithDelay) {
            sender.sendMessage(Component.text("You can't pause the event while a game is active.")
                    .color(NamedTextColor.RED));
            return;
        }
        if (voteManager.isVoting()) {
            sender.sendMessage(Component.text("You can't pause the event during the voting phase.")
                    .color(NamedTextColor.RED));
            return;
        }
        eventPaused = true;
        hubManager.pauseHubTimer();
        Component pauseMessage = Component.text("Event paused.");
        sender.sendMessage(pauseMessage);
        if (!sender.equals(eventMaster)) {
            eventMaster.sendMessage(pauseMessage);
        }
    }
    
    public void resumeEvent(CommandSender sender) {
        if (!eventActive) {
            sender.sendMessage(Component.text("There isn't an event going on.")
                    .color(NamedTextColor.RED));
            return;
        }
        if (!eventPaused) {
            sender.sendMessage(Component.text("The event is not paused.")
                    .color(NamedTextColor.RED));
            return;
        }
        eventPaused = false;
        hubManager.resumeHubTimer();
        Component pauseMessage = Component.text("Event resumed.");
        sender.sendMessage(pauseMessage);
        if (!sender.equals(eventMaster)) {
            eventMaster.sendMessage(pauseMessage);
        }
    }
    
    public void startGameWithDelay(MCTGames mctGame, CommandSender sender) {
        String gameTitle = ChatColor.BLUE+""+ChatColor.BOLD+MCTGames.getTitle(mctGame);
        startingGameWithDelay = true;
        messageOnlineParticipants(Component.empty()
                .append(Component.text(gameTitle)
                        .decorate(TextDecoration.BOLD))
                .append(Component.text(" was selected"))
                .color(NamedTextColor.BLUE));
        this.startGameWithDelayTaskId = new BukkitRunnable() {
            int count = 5;
            @Override
            public void run() {
                if (count <= 0) {
                    if (eventActive) {
                        startGame(mctGame, eventMaster);
                    } else {
                        startGame(mctGame, sender);
                    }
                    startingGameWithDelay = false;
                    this.cancel();
                    return;
                }
                String timeString = TimeStringUtils.getTimeString(count);
                for (Player voter : onlineParticipants) {
                    updateStartGameDelayFastBoard(voter, gameTitle, timeString);
                }
                count--;
            }
        }.runTaskTimer(plugin, 0L, 20L).getTaskId();
    }
    
    /**
     * Starts the given game using the eventMaster as the sender
     * @param mctGame the game to start
     */
    public void startGame(MCTGames mctGame) {
        startGame(mctGame, eventMaster);
    }
    
    /**
     * Starts the given game
     * @param mctGame The game to start
     * @param sender The sender to send messages and alerts to
     */
    public void startGame(MCTGames mctGame, @NotNull CommandSender sender) {
        
        if (voteManager.isVoting()) {
            sender.sendMessage(Component.text("Can't start a game while a vote is going on.")
                    .color(NamedTextColor.RED));
            return;
        }
        
        if (activeGame != null) {
            sender.sendMessage("There is already a game running. You must stop the game before you start a new one.");
            return;
        }
        
        if (onlineParticipants.isEmpty()) {
            sender.sendMessage(Component.text("There are no online participants. You can add participants using:\n")
                    .append(Component.text("/mct team join <team> <member>")
                            .decorate(TextDecoration.BOLD)
                            .clickEvent(ClickEvent.suggestCommand("/mct team join "))));
            return;
        }
        
        List<String> onlineTeams = getTeamNames(onlineParticipants);
        
        switch (mctGame) {
            case FOOT_RACE -> {
                hubManager.removeParticipantsFromHub(onlineParticipants);
                footRaceGame.start(onlineParticipants);
                activeGame = footRaceGame;
            }
            case MECHA -> {
                if (onlineTeams.size() < 2) {
                    sender.sendMessage(Component.text("MECHA doesn't end correctly unless there are 2 or more teams online. use ")
                            .append(Component.text("/mct game stop")
                                    .clickEvent(ClickEvent.suggestCommand("/mct game stop"))
                                    .decorate(TextDecoration.BOLD))
                            .append(Component.text(" to stop the game."))
                            .color(NamedTextColor.RED));
                }
                hubManager.removeParticipantsFromHub(onlineParticipants);
                mechaGame.start(onlineParticipants);
                activeGame = mechaGame;
            }
            case CAPTURE_THE_FLAG -> {
                if (onlineTeams.size() < 2 || 8 < onlineTeams.size()) {
                    sender.sendMessage(Component.text("Capture the Flag needs at least 2 and at most 8 teams online to play.").color(NamedTextColor.RED));
                    return;
                }
                hubManager.removeParticipantsFromHub(onlineParticipants);
                captureTheFlagGame.start(onlineParticipants);
                activeGame = captureTheFlagGame;
            }
            case SPLEEF -> {
                hubManager.removeParticipantsFromHub(onlineParticipants);
                spleefGame.start(onlineParticipants);
                activeGame = spleefGame;
            }
            case PARKOUR_PATHWAY -> {
                hubManager.removeParticipantsFromHub(onlineParticipants);
                parkourPathwayGame.start(onlineParticipants);
                activeGame = parkourPathwayGame;
            }
            case CLOCKWORK -> {
                hubManager.removeParticipantsFromHub(onlineParticipants);
                clockworkGame.start(onlineParticipants);
                activeGame = clockworkGame;
            }
            case COLOSSAL_COLOSSEUM -> {
                if (firstPlaceTeamName == null || secondPlaceTeamName == null) {
                    sender.sendMessage(Component.text("Please specify the first and second place teams.").color(NamedTextColor.RED));
                    return;
                }
                
                List<Player> firstPlaceParticipants = new ArrayList<>();
                List<Player> secondPlaceParticipants = new ArrayList<>();
                List<Player> spectators = new ArrayList<>();
                for (Player participant : onlineParticipants) {
                    String teamName = getTeamName(participant.getUniqueId());
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
    }
    
    /**
     * Meant to be called by the active game when the game is over.
     */
    public void gameIsOver() {
        if (eventActive) {
            try {
                gameStateStorageUtil.addPlayedGame(activeGame.getType());
            } catch (IOException e) {
                eventMaster.sendMessage(Component.text(
                            "Error saving a played game. See log for error message.")
                        .color(NamedTextColor.RED));
                Bukkit.getLogger().severe("Error saving a played game. See log for error message.");
                throw new RuntimeException(e);
            }
            currentGameNumber++;
            eventMaster.sendMessage(Component.text("Now on game ")
                    .append(Component.text(currentGameNumber))
                    .append(Component.text("/"))
                    .append(Component.text(maxGames))
                    .append(Component.text(".")));
        }
        activeGame = null;
        if (!shouldTeleportToHub) {
            shouldTeleportToHub = true;
            return;
        }
        hubManager.returnParticipantsToHubWithDelay(onlineParticipants);
    }
    
    public void finalGameIsOver(String winningTeamName) {
        Bukkit.getLogger().info("Final game is over");
        finalGameEndTaskId = new BukkitRunnable() {
            @Override
            public void run() {
                activeGame = null;
                String winningTeam = gameStateStorageUtil.getTeamDisplayName(winningTeamName);
                List<Player> winningTeamParticipants = getOnlinePlayersOnTeam(winningTeamName);
                String colorString = gameStateStorageUtil.getTeamColorString(winningTeamName);
                ChatColor chatColor = ColorMap.getChatColor(colorString);
                List<Player> otherParticipants = new ArrayList<>();
                for (Player player : onlineParticipants) {
                    if (!winningTeamParticipants.contains(player)) {
                        otherParticipants.add(player);
                    }
                }
                eventActive = false;
                eventMaster = null;
                hubManager.sendParticipantsToPedestal(winningTeamParticipants, winningTeam, chatColor, otherParticipants);
            }
        }.runTaskLater(plugin, 5*20).getTaskId();
    }
    
    //====================================================
    // GameStateStorageUtil accessors and helpers
    //====================================================
    
    /**
     * Remove the given team from the game
     * @param teamName The internal name of the team to remove
     * @return True if the team was successfully removed, false if the team did not exist
     */
    public boolean removeTeam(String teamName) {
        if (!gameStateStorageUtil.containsTeam(teamName)) {
            return false;
        }
        leavePlayersOnTeam(teamName);
        try {
            gameStateStorageUtil.removeTeam(teamName);
        } catch (IOException e) {
            reportGameStateIOException("removing team", e);
        }
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
     * @return True if the team was successfully created. False if the team already exists, or if the name matches the admin team's name.
     */
    public boolean addTeam(String teamName, String teamDisplayName, String colorString) {
        if (gameStateStorageUtil.containsTeam(teamName)) {
            return false;
        }
        try {
            gameStateStorageUtil.addTeam(teamName, teamDisplayName, colorString);
        } catch (IOException e) {
            reportGameStateIOException("adding score to player", e);
        }
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
     * to the game state. Note, this will not join a player to a team
     * if that player is an admin. 
     * @param player The player to join to the given team
     * @param teamName The internal teamName of the team to join the player to. 
     *                 This method assumes the team exists, and will throw a 
     *                 null pointer exception if it doesn't.
     */
    public void joinPlayerToTeam(Player player, String teamName) {
        UUID playerUniqueId = player.getUniqueId();
        if (isAdmin(playerUniqueId)) {
            return;
        }
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
     */
    private void addNewPlayer(UUID playerUniqueId, String teamName) {
        try {
            gameStateStorageUtil.addNewPlayer(playerUniqueId, teamName);
        } catch (IOException e) {
            reportGameStateIOException("adding new player", e);
        }
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
     */
    public void leavePlayer(OfflinePlayer offlinePlayer) {
        if (offlinePlayer.isOnline()) {
            Player onlinePlayer = offlinePlayer.getPlayer();
            if (onlinePlayer != null) {
                onParticipantQuit(onlinePlayer);
            }
        }
        UUID playerUniqueId = offlinePlayer.getUniqueId();
        String teamName = gameStateStorageUtil.getPlayerTeamName(playerUniqueId);
        try {
            gameStateStorageUtil.leavePlayer(playerUniqueId);
        } catch (IOException e) {
            reportGameStateIOException("leaving player", e);
        }
        Team team = mctScoreboard.getTeam(teamName);
        team.removePlayer(offlinePlayer);
        fastBoardManager.removeBoard(playerUniqueId);
    }
    
    private void leavePlayersOnTeam(String teamName) {
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
        String teamName = gameStateStorageUtil.getPlayerTeamName(playerUniqueId);
        addScore(playerUniqueId, points);
        addScore(teamName, points);
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
        List<String> names = new ArrayList<>();
        for (Player player : onlineParticipants) {
            names.add(player.getName());
        }
        return names;
    }
    
    
    /**
     * Gets all the names of the participants in the game state, regardless of 
     * whether they're offline or online. 
     * @return a list of the names of all participants in the game state
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
     */
    public void addScore(UUID participantUniqueId, int score) {
        try {
            gameStateStorageUtil.addScore(participantUniqueId, score);
        } catch (IOException e) {
            reportGameStateIOException("adding score to player", e);
        }
    }
    
    /**
     * Adds the given score to the team with the given name
     * @param teamName The name of the team to add the score to
     * @param score The score to add. Could be positive or negative.
     */
    public void addScore(String teamName, int score) {
        try {
            gameStateStorageUtil.addScore(teamName, score);
        } catch (IOException e) {
            reportGameStateIOException("adding score to team", e);
        }
    }
    
    /**
     * Sets the score of the participant with the given UUID to the given value
     * @param participantUniqueId The UUID of the participant to set the score to
     * @param score The score to set to. If the score is negative, the score will be set to 0.
     */
    public void setScore(UUID participantUniqueId, int score) {
        try {
            if (score < 0) {
                gameStateStorageUtil.setScore(participantUniqueId, 0);
                return;
            }
            gameStateStorageUtil.setScore(participantUniqueId, score);
        } catch (IOException e) {
            reportGameStateIOException("setting a player's score", e);
        }
    }
    
    /**
     * Sets the score of the team with the given name to the given value
     * @param teamName The UUID of the participant to set the score to
     * @param score The score to set to. If the score is negative, the score will be set to 0.
     */
    public void setScore(String teamName, int score) {
        try {
            if (score < 0) {
                gameStateStorageUtil.setScore(teamName, 0);
                return;
            }
            gameStateStorageUtil.setScore(teamName, score);
        } catch (IOException e) {
            reportGameStateIOException("adding score to team", e);
        }
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
    
    
    /**
     * Checks if the given player is an admin
     * @param adminUniqueId The unique id of the admin to check
     * @return True if the given player is an admin, false otherwise
     */
    public boolean isAdmin(UUID adminUniqueId) {
        return gameStateStorageUtil.isAdmin(adminUniqueId);
    }
    
    /**
     * Adds the given player as an admin
     * @param newAdmin The player to add
     */
    public void addAdmin(Player newAdmin) {
        UUID uniqueId = newAdmin.getUniqueId();
        if (gameStateStorageUtil.isAdmin(uniqueId)) {
            return;
        }
        try {
            gameStateStorageUtil.addAdmin(uniqueId);
        } catch (IOException e) {
            reportGameStateIOException("adding new admin", e);
        }
        Team adminTeam = mctScoreboard.getTeam(ADMIN_TEAM);
        adminTeam.addPlayer(newAdmin);
        if (newAdmin.isOnline()) {
            onAdminJoin(newAdmin);
        }
    }
    
    /**
     * Removes the given player from the admins
     * @param offlineAdmin The admin to remove
     */
    public void removeAdmin(OfflinePlayer offlineAdmin) {
        if (offlineAdmin.isOnline()) {
            Player onlineAdmin = offlineAdmin.getPlayer();
            if (onlineAdmin != null) {
                onAdminQuit(onlineAdmin);
            }
        }
        UUID adminUniqueId = offlineAdmin.getUniqueId();
        try {
            gameStateStorageUtil.removeAdmin(adminUniqueId);
        } catch (IOException e) {
            reportGameStateIOException("removing admin", e);
        }
        Team team = mctScoreboard.getTeam(ADMIN_TEAM);
        team.removePlayer(offlineAdmin);
        fastBoardManager.removeBoard(adminUniqueId);
    }
    
    public void setFinalGameTeams(String teamA, String teamB) {
        this.firstPlaceTeamName = teamA;
        this.secondPlaceTeamName = teamB;
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
    
    public void setBoundaryEnabled(boolean boundaryEnabled) {
        hubManager.setBoundaryEnabled(boundaryEnabled);
    }
    
    public boolean eventIsActive() {
        return eventActive;
    }
    
    public int getCurrentGameNumber() {
        return currentGameNumber;
    }
    
    public int getMaxGames() {
        return maxGames;
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
    
    public void messageAdmins(Component message) {
        for (Player admin : onlineAdmins) {
            admin.sendMessage(message);
        }
    }
    
    public void playSoundForAdmins(Sound sound, int volume, int pitch) {
        for (Player admin : onlineAdmins) {
            admin.playSound(admin.getLocation(), sound, volume, pitch);
        }
    }
    
    
    private void messageOnlineParticipants(Component message) {
        for (Player participant : onlineParticipants) {
            participant.sendMessage(message);
        }
    }
    
    // Test methods
    
    public void setFastBoardManager(FastBoardManager fastBoardManager) {
        this.fastBoardManager = fastBoardManager;
        this.fastBoardManager.setGameStateStorageUtil(this.gameStateStorageUtil);
    }
    
    public void setGameStateStorageUtil(GameStateStorageUtil gameStateStorageUtil) {
        this.gameStateStorageUtil = gameStateStorageUtil;
        this.fastBoardManager.setGameStateStorageUtil(gameStateStorageUtil);
    }
    
    private void reportGameStateIOException(String attemptedOperation, IOException ioException) {
        Bukkit.getLogger().severe(String.format("error while %s. See console log for error message.", attemptedOperation));
        messageAdmins(Component.empty()
                .append(Component.text("error while "))
                .append(Component.text(attemptedOperation))
                .append(Component.text(". See console log for error message.")));
        throw new RuntimeException(ioException);
    }
    
    public MCTGame getActiveGame() {
        return activeGame;
    }
}
