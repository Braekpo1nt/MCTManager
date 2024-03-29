package org.braekpo1nt.mctmanager.games;

import com.google.common.base.Preconditions;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.games.game.clockwork.ClockworkGame;
import org.braekpo1nt.mctmanager.games.game.enums.GameType;
import org.braekpo1nt.mctmanager.games.event.EventManager;
import org.braekpo1nt.mctmanager.games.game.interfaces.Configurable;
import org.braekpo1nt.mctmanager.games.game.interfaces.GameEditor;
import org.braekpo1nt.mctmanager.games.game.parkourpathway.editor.ParkourPathwayEditor;
import org.braekpo1nt.mctmanager.ui.sidebar.Headerable;
import org.braekpo1nt.mctmanager.ui.sidebar.SidebarFactory;
import org.braekpo1nt.mctmanager.utils.ColorMap;
import org.braekpo1nt.mctmanager.games.game.capturetheflag.CaptureTheFlagGame;
import org.braekpo1nt.mctmanager.games.game.footrace.FootRaceGame;
import org.braekpo1nt.mctmanager.games.gamestate.GameStateStorageUtil;
import org.braekpo1nt.mctmanager.games.game.interfaces.MCTGame;
import org.braekpo1nt.mctmanager.games.game.mecha.MechaGame;
import org.braekpo1nt.mctmanager.games.game.parkourpathway.ParkourPathwayGame;
import org.braekpo1nt.mctmanager.games.game.spleef.SpleefGame;
import org.braekpo1nt.mctmanager.games.voting.VoteManager;
import org.braekpo1nt.mctmanager.hub.HubManager;
import org.bukkit.*;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
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
    public static final NamedTextColor ADMIN_COLOR = NamedTextColor.DARK_RED; 
    private MCTGame activeGame = null;
    private GameEditor activeEditor = null;
    private final Map<GameType, MCTGame> games;
    private final Map<GameType, GameEditor> editors;
    private final HubManager hubManager;
    private SidebarFactory sidebarFactory;
    private GameStateStorageUtil gameStateStorageUtil;
    /**
     * Scoreboard for holding the teams. This private scoreboard can't be
     * modified using the normal /team command, and thus can't be unsynced
     * with the game state.
     */
    private final Scoreboard mctScoreboard;
    private boolean shouldTeleportToHub = true;
    private final VoteManager voteManager;
    private final EventManager eventManager;
    /**
     * Contains the list of online participants. Updated when participants are added/removed or quit/join
     */
    private final List<Player> onlineParticipants = new ArrayList<>();
    private final List<Player> onlineAdmins = new ArrayList<>();
    
    public GameManager(Main plugin, Scoreboard mctScoreboard) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        this.mctScoreboard = mctScoreboard;
        this.gameStateStorageUtil = new GameStateStorageUtil(plugin);
        this.voteManager = new VoteManager(this, plugin);
        this.games = new HashMap<>();
        addGame(new FootRaceGame(plugin, this));
        addGame(new MechaGame(plugin, this));
        addGame(new SpleefGame(plugin, this));
        addGame(new ParkourPathwayGame(plugin, this));
        addGame(new CaptureTheFlagGame(plugin, this));
        addGame(new ClockworkGame(plugin, this));
        this.editors = new HashMap<>();
        addEditor(new ParkourPathwayEditor(plugin));
        this.sidebarFactory = new SidebarFactory();
        this.hubManager = new HubManager(plugin, this);
        hubManager.initializeSidebar(sidebarFactory);
        this.eventManager = new EventManager(plugin, this, voteManager);
    }
    
    /**
     * Adds the given game to the games map, with the key being the game's type
     * @param mctGame the {@link MCTGame} implementation
     * @throws IllegalArgumentException if you attempt to add a game whose {@link GameType} was already added to the games list
     */
    private void addGame(MCTGame mctGame) {
        Preconditions.checkArgument(!this.games.containsKey(mctGame.getType()), "A game with type %s already exists in the games map", mctGame.getType());
        this.games.put(mctGame.getType(), mctGame);
    }
    
    /**
     * Adds the given editor to the editors map, with the key being the editor's type
     * @param editor the {@link GameEditor} implementation
     * @throws IllegalArgumentException if you attempt to add an editor whose {@link GameType} was already added to the editors list
     */
    private void addEditor(GameEditor editor) {
        Preconditions.checkArgument(!this.editors.containsKey(editor.getType()), "An editor with type %s already exists in the games map", editor.getType());
        this.editors.put(editor.getType(), editor);
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
        if (gameIsRunning()) {
            activeGame.onAdminQuit(admin);
        } else if (eventManager.eventIsActive() || eventManager.colossalCombatIsActive()) {
            eventManager.onAdminQuit(admin);
        } else if (voteManager.isVoting()) {
            voteManager.onAdminQuit(admin);
        }
        hubManager.onAdminQuit(admin);
        Component displayName = Component.text(admin.getName(), NamedTextColor.WHITE);
        admin.displayName(displayName);
        admin.playerListName(displayName);
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
        if (gameIsRunning()) {
            activeGame.onParticipantQuit(participant);
        } else if (eventManager.eventIsActive() || eventManager.colossalCombatIsActive()) {
            eventManager.onParticipantQuit(participant);
        } else if (voteManager.isVoting()) {
            voteManager.onParticipantQuit(participant);
        }
        hubManager.onParticipantQuit(participant);
        Component displayName = Component.text(participant.getName(), NamedTextColor.WHITE);
        participant.displayName(displayName);
        participant.playerListName(displayName);
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
        admin.setScoreboard(mctScoreboard);
        admin.addPotionEffect(Main.NIGHT_VISION);
        Component displayName = Component.empty()
                .append(Component.text("["))
                .append(Component.text("Admin")
                        .color(ADMIN_COLOR))
                .append(Component.text("] "))
                .append(Component.text(admin.getName()));
        admin.displayName(displayName);
        admin.playerListName(displayName);
        hubManager.onAdminJoin(admin);
        if (gameIsRunning()) {
            activeGame.onAdminJoin(admin);
        } else if (eventManager.eventIsActive() || eventManager.colossalCombatIsActive()) {
            eventManager.onAdminJoin(admin);
        } else if (voteManager.isVoting()) {
            voteManager.onAdminJoin(admin);
        }
    }
    
    /**
     * Handles when a participant joins
     * @param participant a player (who is an official participant) who joined
     */
    private void onParticipantJoin(@NotNull Player participant) {
        onlineParticipants.add(participant);
        participant.setScoreboard(mctScoreboard);
        participant.addPotionEffect(Main.NIGHT_VISION);
        String teamName = getTeamName(participant.getUniqueId());
        NamedTextColor teamNamedTextColor = getTeamNamedTextColor(teamName);
        Component displayName = Component.text(participant.getName(), teamNamedTextColor);
        participant.displayName(displayName);
        participant.playerListName(displayName);
        hubManager.onParticipantJoin(participant);
        if (gameIsRunning()) {
            hubManager.removeParticipantsFromHub(Collections.singletonList(participant));
            activeGame.onParticipantJoin(participant);
        } else if (eventManager.eventIsActive() || eventManager.colossalCombatIsActive()) {
            hubManager.removeParticipantsFromHub(Collections.singletonList(participant));
            eventManager.onParticipantJoin(participant);
        } else if (voteManager.isVoting()) {
            voteManager.onParticipantJoin(participant);
        }
        updateTeamScore(teamName);
        updatePersonalScore(participant);
    }
    
    public Scoreboard getMctScoreboard() {
        return mctScoreboard;
    }
    
    public SidebarFactory getSidebarFactory() {
        return sidebarFactory;
    }
    
    /**
     * Load the hub config
     * @return true if the config succeeded in loaded
     * @throws IllegalArgumentException if any problem occurred loading the hub config
     */
    public boolean loadHubConfig() throws IllegalArgumentException {
        return hubManager.loadConfig();
    }
    
    /**
     * Attempts to load the config for the active game. If false is returned, no change was made and nothing happens other than error messages are sent to the sender.
     * @param sender the sender
     * @return false if there is no game running, the game is not configurable, or the config could not be loaded. true if the config was loaded.
     */
    public boolean loadGameConfig(CommandSender sender) {
        if (!gameIsRunning()) {
            sender.sendMessage(Component.text("No game is running.")
                    .color(NamedTextColor.RED));
            return false;
        }
        
        if (!(activeGame instanceof Configurable configurable)) {
            sender.sendMessage(Component.text("This game is not configurable.")
                    .color(NamedTextColor.RED));
            return false;
        }
        
        try {
            if (!configurable.loadConfig()) {
                throw new IllegalArgumentException("Config could not be loaded.");
            }
        } catch (IllegalArgumentException e) {
            Bukkit.getLogger().severe(e.getMessage());
            e.printStackTrace();
            sender.sendMessage(Component.text("Error loading config file for ")
                    .append(Component.text(activeGame.getType().name())
                            .decorate(TextDecoration.BOLD))
                    .append(Component.text(". See console for details:\n"))
                    .append(Component.text(e.getMessage()))
                    .color(NamedTextColor.RED));
            return false;
        }
        
        sender.sendMessage(Component.text("Game config was loaded.")
                .color(NamedTextColor.GREEN));
        return true;
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
                onAdminJoin(player);
            }
            if (gameStateStorageUtil.containsPlayer(player.getUniqueId())) {
                onParticipantJoin(player);
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
    public void manuallyStartVote(@NotNull CommandSender sender, List<GameType> votingPool, int duration) {
        if (gameIsRunning()) {
            sender.sendMessage(Component.text("There is a game running. You must stop the game before you start a vote.")
                    .color(NamedTextColor.RED));
            return;
        }
        if (editorIsRunning()) {
            sender.sendMessage(Component.text("There is an editor running. You must stop the editor before you start a vote.")
                    .color(NamedTextColor.RED));
            return;
        }
        if (onlineParticipants.isEmpty()) {
            sender.sendMessage(Component.text("There are no online participants. You can add participants using:\n")
                    .append(Component.text("/mct team join <team> <member>")
                            .decorate(TextDecoration.BOLD)
                            .clickEvent(ClickEvent.suggestCommand("/mct team join "))));
            return;
        }
        voteManager.startVote(onlineParticipants, votingPool, duration, (gameType) -> startGame(gameType, sender), onlineAdmins);
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
        eventManager.cancelAllTasks();
        hubManager.cancelAllTasks();
    }
    
    public EventManager getEventManager() {
        return eventManager;
    }
    
    public void removeParticipantsFromHub(List<Player> participantsToRemove) {
        hubManager.removeParticipantsFromHub(participantsToRemove);
    }
    
    /**
     * Starts the given game
     * @param gameType The game to start
     * @param sender The sender to send messages and alerts to
     * @return true if the game started successfully, false otherwise
     */
    public boolean startGame(GameType gameType, @NotNull CommandSender sender) {
        if (voteManager.isVoting()) {
            sender.sendMessage(Component.text("Can't start a game while a vote is going on.")
                    .color(NamedTextColor.RED));
            return false;
        }
        
        if (gameIsRunning()) {
            sender.sendMessage(Component.text("There is already a game running. You must stop the game before you start a new one.")
                    .color(NamedTextColor.RED));
            return false;
        }
        
        if (editorIsRunning()) {
            sender.sendMessage(Component.text("There is an editor running. You must stop the editor before you start a game.")
                    .color(NamedTextColor.RED));
            return false;
        }
        
        if (onlineParticipants.isEmpty()) {
            sender.sendMessage(Component.text("There are no online participants. You can add participants using:\n")
                    .append(Component.text("/mct team join <team> <member>")
                            .decorate(TextDecoration.BOLD)
                            .clickEvent(ClickEvent.suggestCommand("/mct team join "))));
            return false;
        }
        
        MCTGame selectedGame = this.games.get(gameType);
        if (selectedGame == null) {
            sender.sendMessage(Component.text("Can't find game for type " + gameType));
            return false;
        }
        
        // make sure config loads
        if (selectedGame instanceof Configurable configurable) {
            try {
                if (!configurable.loadConfig()) {
                    throw new IllegalArgumentException("Config could not be loaded.");
                }
            } catch (IllegalArgumentException e) {
                Bukkit.getLogger().severe(e.getMessage());
                e.printStackTrace();
                Component message = Component.text("Can't start ")
                        .append(Component.text(gameType.name())
                                .decorate(TextDecoration.BOLD))
                        .append(Component.text(". Error loading config file. See console for details:\n"))
                        .append(Component.text(e.getMessage()))
                        .color(NamedTextColor.RED);
                sender.sendMessage(message);
                messageAdmins(message);
                return false;
            }
        }
        
        List<String> onlineTeams = getTeamNames(onlineParticipants);
        // make sure the player and team count requirements are met
        switch (gameType) {
            case MECHA -> {
                if (onlineTeams.size() < 2) {
                    sender.sendMessage(Component.text("MECHA doesn't end correctly unless there are 2 or more teams online. use ")
                            .append(Component.text("/mct game stop")
                                    .clickEvent(ClickEvent.suggestCommand("/mct game stop"))
                                    .decorate(TextDecoration.BOLD))
                            .append(Component.text(" to stop the game."))
                            .color(NamedTextColor.RED));
                }
            }
            case CAPTURE_THE_FLAG -> {
                if (onlineTeams.size() < 2 || 8 < onlineTeams.size()) {
                    sender.sendMessage(Component.text("Capture the Flag needs at least 2 and at most 8 teams online to play.").color(NamedTextColor.RED));
                    return false;
                }
            }
        }
        
        hubManager.removeParticipantsFromHub(onlineParticipants);
        selectedGame.start(onlineParticipants, onlineAdmins);
        activeGame = selectedGame;
        for (String teamName : getTeamNames(onlineParticipants)) {
            updateTeamScore(teamName);
        }
        for (Player participant : onlineParticipants) {
            updatePersonalScore(participant);
        }
        return true;
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
     * If an event is running, calls {@link EventManager#gameIsOver(GameType)}
     */
    public void gameIsOver() {
        if (eventManager.eventIsActive()) {
            eventManager.gameIsOver(activeGame.getType());
            activeGame = null;
            return;
        }
        activeGame = null;
        if (!shouldTeleportToHub) {
            shouldTeleportToHub = true;
            return;
        }
        hubManager.returnParticipantsToHub(onlineParticipants, onlineAdmins, true);
    }
    
    public boolean startEditor(GameType gameType, @NotNull CommandSender sender) {
        if (voteManager.isVoting()) {
            sender.sendMessage(Component.text("Can't start a game while a vote is going on.")
                    .color(NamedTextColor.RED));
            return false;
        }
        
        if (gameIsRunning()) {
            sender.sendMessage(Component.text("There is a game running. You must stop the game before you start an editor.")
                    .color(NamedTextColor.RED));
            return false;
        }
        
        if (eventManager.eventIsActive()) {
            sender.sendMessage(Component.text("Can't start an editor while an event is going on")
                    .color(NamedTextColor.RED));
            return false;
        }
        
        if (eventManager.colossalCombatIsActive()) {
            sender.sendMessage(Component.text("Can't start an editor while colossal combat is running")
                    .color(NamedTextColor.RED));
            return false;
        }
        
        if (onlineParticipants.isEmpty()) {
            sender.sendMessage(Component.text("There are no online participants. You can add participants using:\n")
                    .append(Component.text("/mct team join <team> <member>")
                            .decorate(TextDecoration.BOLD)
                            .clickEvent(ClickEvent.suggestCommand("/mct team join "))));
            return false;
        }
        
        if (editorIsRunning()) {
            sender.sendMessage(Component.text("An editor is already running. You must stop it before you can start another one.")
                    .color(NamedTextColor.RED));
            return false;
        }
        
        GameEditor selectedEditor = this.editors.get(gameType);
        if (selectedEditor == null) {
            sender.sendMessage(Component.text("Can't find editor for game type " + gameType)
                    .color(NamedTextColor.RED));
            return false;
        }
        
        // make sure config loads
        try {
            if (!selectedEditor.loadConfig()) {
                throw new IllegalArgumentException("Config could not be loaded.");
            }
        } catch (IllegalArgumentException e) {
            Bukkit.getLogger().severe(e.getMessage());
            e.printStackTrace();
            Component message = Component.text("Can't start ")
                    .append(Component.text(gameType.name())
                            .decorate(TextDecoration.BOLD))
                    .append(Component.text(". Error loading config file. See console for details:\n"))
                    .append(Component.text(e.getMessage()))
                    .color(NamedTextColor.RED);
            sender.sendMessage(message);
            messageAdmins(message);
            return false;
        }
        
        selectedEditor.start(onlineParticipants);
        activeEditor = selectedEditor;
        return true;
    }
    
    public void stopEditor(@NotNull CommandSender sender) {
        if (!editorIsRunning()) {
            sender.sendMessage(Component.text("No editor is running.")
                    .color(NamedTextColor.RED));
            return;
        }
        activeEditor.stop();
        activeEditor = null;
    }
    
    public boolean editorIsRunning() {
        return activeEditor != null;
    }
    
    public void validateEditor(@NotNull CommandSender sender) {
        if (!editorIsRunning()) {
            sender.sendMessage(Component.text("No editor is running.")
                    .color(NamedTextColor.RED));
            return;
        }
        try {
            if (!activeEditor.configIsValid()) {
                throw new IllegalArgumentException("Config is not valid");
            }
        } catch (IllegalArgumentException e) {
            Bukkit.getLogger().severe(e.getMessage());
            e.printStackTrace();
            sender.sendMessage(Component.text("Config is not valid for ")
                    .append(Component.text(activeEditor.getType().name())
                            .decorate(TextDecoration.BOLD))
                    .append(Component.text(". See console for details:\n"))
                    .append(Component.text(e.getMessage()))
                    .color(NamedTextColor.RED));
            return;
        }
        sender.sendMessage(Component.text("Config is valid.")
                .color(NamedTextColor.GREEN));
    }
    
    /**
     * @param sender the sender
     * @param force if true, validation will be skipped and the config will be saved even if invalid, if false the config will only save if it is valid
     */
    public void saveEditor(@NotNull CommandSender sender, boolean force) {
        if (!editorIsRunning()) {
            sender.sendMessage(Component.text("No editor is running.")
                    .color(NamedTextColor.RED));
            return;
        }
        if (!force) {
            try {
                if (!activeEditor.configIsValid()) {
                    throw new IllegalArgumentException("Config is not valid");
                }
            } catch (IllegalArgumentException e) {
                Bukkit.getLogger().severe(e.getMessage());
                e.printStackTrace();
                sender.sendMessage(Component.text("Config is not valid for ")
                        .append(Component.text(activeEditor.getType().name())
                                .decorate(TextDecoration.BOLD))
                        .append(Component.text(". See console for details:\n"))
                        .append(Component.text(e.getMessage()))
                        .color(NamedTextColor.RED));
                sender.sendMessage(Component.text("Skipping save. If you wish to force the save, use ")
                        .append(Component.text("/mct edit save true")
                                .clickEvent(ClickEvent.suggestCommand("/mct edit save true"))
                                .decorate(TextDecoration.BOLD))
                        .color(NamedTextColor.RED));
                return;
            }
        } else {
            sender.sendMessage("Skipping validation.");
        }
        try {
            activeEditor.saveConfig();
        } catch (IOException e) {
            Bukkit.getLogger().severe(e.getMessage());
            e.printStackTrace();
            sender.sendMessage(Component.text("An error occurred while attempting to save the config for ")
                    .append(Component.text(activeEditor.getType().name())
                            .decorate(TextDecoration.BOLD))
                    .append(Component.text(". See console for details:\n"))
                    .append(Component.text(e.getMessage()))
                    .color(NamedTextColor.RED));
            return;
        }
        sender.sendMessage(Component.text("Config is saved.")
                .color(NamedTextColor.GREEN));
    }
    
    public void loadEditor(@NotNull CommandSender sender) {
        try {
            if (!activeEditor.loadConfig()) {
                throw new IllegalArgumentException("Config could not be loaded.");
            }
        } catch (IllegalArgumentException e) {
            Bukkit.getLogger().severe(e.getMessage());
            e.printStackTrace();
            Component message = Component.text("Can't start ")
                    .append(Component.text(activeEditor.getType().name())
                            .decorate(TextDecoration.BOLD))
                    .append(Component.text(". Error loading config file. See console for details:\n"))
                    .append(Component.text(e.getMessage()))
                    .color(NamedTextColor.RED);
            sender.sendMessage(message);
        }
        sender.sendMessage(Component.text("Config loaded.")
                .color(NamedTextColor.GREEN));
    }
    
    public void returnAllParticipantsToHub() {
        hubManager.returnParticipantsToHub(onlineParticipants, onlineAdmins, false);
    }
    
    /**
     * Instantly returns the given participant to the hub
     * @param participant the participant to be returned to the hub
     */
    public void returnParticipantToHubInstantly(Player participant) {
        hubManager.returnParticipantsToHub(Collections.singletonList(participant), Collections.emptyList(), false);
    }
    
    public void returnAllParticipantsToPodium(String winningTeam) {
        List<Player> winningTeamParticipants = getOnlinePlayersOnTeam(winningTeam);
        List<Player> otherParticipants = new ArrayList<>();
        for (Player participant : getOnlineParticipants()) {
            if (!winningTeamParticipants.contains(participant)) {
                otherParticipants.add(participant);
            }
        }
        hubManager.sendAllParticipantsToPodium(winningTeamParticipants, otherParticipants, onlineAdmins);
    }
    
    public void returnParticipantToPodium(Player participant, boolean winner) {
        hubManager.sendParticipantToPodium(participant, winner);
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
        if (eventManager.eventIsActive()) {
            eventManager.updateTeamScores();
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
        updateTeamScore(teamName);
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
     * @param participant The player to join to the given team
     * @param teamName The internal teamName of the team to join the player to. 
     *                 This method assumes the team exists, and will throw a 
     *                 null pointer exception if it doesn't.
     */
    public void joinPlayerToTeam(Player participant, String teamName) {
        UUID playerUniqueId = participant.getUniqueId();
        if (isAdmin(playerUniqueId)) {
            return;
        }
        if (gameStateStorageUtil.containsPlayer(playerUniqueId)) {
            String originalTeamName = getTeamName(playerUniqueId);
            if (originalTeamName.equals(teamName)) {
                return;
            }
            movePlayerToTeam(playerUniqueId, teamName);
            participant.sendMessage(Component.text("You've been moved to ")
                    .append(getFormattedTeamDisplayName(teamName)));
        } else {
            addNewPlayer(playerUniqueId, teamName);
            participant.sendMessage(Component.text("You've been joined to ")
                    .append(getFormattedTeamDisplayName(teamName)));
        }
        NamedTextColor teamNamedTextColor = getTeamNamedTextColor(teamName);
        Component displayName = Component.text(participant.getName(), teamNamedTextColor);
        participant.displayName(displayName);
        participant.playerListName(displayName);
    }
    
    private void movePlayerToTeam(UUID playerUniqueId, String newTeamName) {
        String oldTeamName = gameStateStorageUtil.getPlayerTeamName(playerUniqueId);
        gameStateStorageUtil.setPlayerTeamName(playerUniqueId, newTeamName);
    
        OfflinePlayer player = Bukkit.getOfflinePlayer(playerUniqueId);
        Team oldTeam = mctScoreboard.getTeam(oldTeamName);
        Preconditions.checkState(oldTeam != null, "mctScoreboard could not find team \"%s\"", oldTeamName);
        oldTeam.removePlayer(player);
        Team newTeam = mctScoreboard.getTeam(newTeamName);
        Preconditions.checkState(newTeam != null, "mctScoreboard could not find team \"%s\"", newTeamName);
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
    
    public List<UUID> getParticipantUUIDsOnTeam(String teamName) {
        return gameStateStorageUtil.getPlayerUniqueIdsOnTeam(teamName);
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
        Preconditions.checkState(team != null, "Something is wrong with the team Scoreboard. Could not find team with name %s", teamName);
        team.addPlayer(newPlayer);
        Player onlineNewPlayer = newPlayer.getPlayer();
        if (onlineNewPlayer != null) {
            onParticipantJoin(onlineNewPlayer);
        }
    }
    
    /**
     * Leaves the player from the team and removes them from the game state.
     * If a game is running, and the player is online, removes that player from the game as well. 
     * @param offlinePlayer The player to remove from the team
     */
    public void leavePlayer(OfflinePlayer offlinePlayer) {
        UUID playerUniqueId = offlinePlayer.getUniqueId();
        String teamName = gameStateStorageUtil.getPlayerTeamName(playerUniqueId);
        if (offlinePlayer.isOnline()) {
            Player onlinePlayer = offlinePlayer.getPlayer();
            if (onlinePlayer != null) {
                onParticipantQuit(onlinePlayer);
                onlinePlayer.sendMessage(Component.text("You've been removed from ")
                        .append(getFormattedTeamDisplayName(teamName)));
            }
        }
        try {
            gameStateStorageUtil.leavePlayer(playerUniqueId);
        } catch (IOException e) {
            reportGameStateIOException("leaving player", e);
        }
        Team team = mctScoreboard.getTeam(teamName);
        Preconditions.checkState(team != null, "mctScoreboard could not find team \"%s\"", teamName);
        team.removePlayer(offlinePlayer);
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
     * Awards points to the participant and their team and announces to that participant how many points they received. 
     * If the participant does not exist, nothing happens.
     * @param participant The participant to award points to
     * @param points The points to award to the participant
     */
    
    public void awardPointsToParticipant(Player participant, int points) {
        UUID participantUUID = participant.getUniqueId();
        if (!gameStateStorageUtil.containsPlayer(participantUUID)) {
            return;
        }
        String teamName = gameStateStorageUtil.getPlayerTeamName(participantUUID);
        int multipliedPoints = (int) (points * eventManager.matchProgressPointMultiplier());
        addScore(participantUUID, multipliedPoints);
        addScore(teamName, multipliedPoints);
        eventManager.trackPoints(participantUUID, multipliedPoints, activeGame.getType());
        eventManager.trackPoints(teamName, multipliedPoints, activeGame.getType());
    
        participant.sendMessage(Component.text("+")
                .append(Component.text(multipliedPoints))
                .append(Component.text(" points"))
                .decorate(TextDecoration.BOLD)
                .color(NamedTextColor.GOLD));
        updateTeamScore(teamName);
        updatePersonalScore(participant);
    }
    
    /**
     * Adds the given points to the given team, and announces to all online members of that 
     * team how many points the team earned.
     * If the team doesn't exist, nothing happens. 
     * @param teamName The team to add points to
     * @param points The points to add
     */
    public void awardPointsToTeam(String teamName, int points) {
        if (!gameStateStorageUtil.containsTeam(teamName)) {
            return;
        }
        int multipliedPoints = (int) (points * eventManager.matchProgressPointMultiplier());
        addScore(teamName, multipliedPoints);
        eventManager.trackPoints(teamName, multipliedPoints, activeGame.getType());
        
        Component displayName = getFormattedTeamDisplayName(teamName);
        List<Player> playersOnTeam = getOnlinePlayersOnTeam(teamName);
        for (Player playerOnTeam : playersOnTeam) {
            playerOnTeam.sendMessage(Component.text("+")
                    .append(Component.text(multipliedPoints))
                    .append(Component.text(" points for "))
                    .append(displayName)
                    .decorate(TextDecoration.BOLD)
                    .color(NamedTextColor.GOLD));
        }
        updateTeamScore(teamName);
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
     * @return a copy of the list of online participants. Modifying this will not change
     * the online participants
     */
    public List<Player> getOnlineParticipants() {
        return new ArrayList<>(onlineParticipants);
    }
    
    /**
     * @return a copy of the list of online admins. Modifying this will not change the online admins. 
     */
    public List<Player> getOnlineAdmins() {
        return new ArrayList<>(onlineAdmins);
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
     * @param participantUUID The UUID of the participant to add the score to
     * @param score The score to add. Could be positive or negative.
     */
    public void addScore(UUID participantUUID, int score) {
        try {
            gameStateStorageUtil.addScore(participantUUID, score);
            Player participant = Bukkit.getPlayer(participantUUID);
            if (participant != null && onlineParticipants.contains(participant)) {
                updatePersonalScore(participant);
            }
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
            updateTeamScore(teamName);
        } catch (IOException e) {
            reportGameStateIOException("adding score to team", e);
        }
    }
    
    /**
     * Sets the score of the participant with the given UUID to the given value
     * @param participantUUID The UUID of the participant to set the score to
     * @param score The score to set to. If the score is negative, the score will be set to 0.
     */
    public void setScore(UUID participantUUID, int score) {
        try {
            if (score < 0) {
                gameStateStorageUtil.setScore(participantUUID, 0);
                return;
            }
            gameStateStorageUtil.setScore(participantUUID, score);
            Player participant = Bukkit.getPlayer(participantUUID);
            if (participant != null && onlineParticipants.contains(participant)) {
                updatePersonalScore(participant);
            }
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
            updateTeamScore(teamName);
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
     * Adds the given player as an admin. If the player is a participant or already an admin, nothing happens.
     * @param newAdmin The player to add
     */
    public void addAdmin(Player newAdmin) {
        UUID uniqueId = newAdmin.getUniqueId();
        if (gameStateStorageUtil.containsPlayer(uniqueId)) {
            return;
        }
        if (gameStateStorageUtil.isAdmin(uniqueId)) {
            return;
        }
        try {
            gameStateStorageUtil.addAdmin(uniqueId);
        } catch (IOException e) {
            reportGameStateIOException("adding new admin", e);
        }
        Team adminTeam = mctScoreboard.getTeam(ADMIN_TEAM);
        Preconditions.checkState(adminTeam != null, "mctScoreboard could not find team \"%s\"", ADMIN_TEAM);
        adminTeam.addPlayer(newAdmin);
        if (newAdmin.isOnline()) {
            newAdmin.sendMessage(Component.text("You were added as an admin")
                    .color(NamedTextColor.YELLOW));
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
                onlineAdmin.sendMessage(Component.text("You were removed as an admin")
                        .color(NamedTextColor.YELLOW));
                onAdminQuit(onlineAdmin);
            }
        }
        UUID adminUniqueId = offlineAdmin.getUniqueId();
        try {
            gameStateStorageUtil.removeAdmin(adminUniqueId);
        } catch (IOException e) {
            reportGameStateIOException("removing admin", e);
        }
        Team adminTeam = mctScoreboard.getTeam(ADMIN_TEAM);
        Preconditions.checkState(adminTeam != null, "mctScoreboard could not find team \"%s\"", ADMIN_TEAM);
        adminTeam.removePlayer(offlineAdmin);
    }
    
    public Material getTeamPowderColor(String teamName) {
        String colorString = gameStateStorageUtil.getTeamColorString(teamName);
        return ColorMap.getConcretePowderColor(colorString);
    }
    
    public Material getTeamConcreteColor(String teamName) {
        String colorString = gameStateStorageUtil.getTeamColorString(teamName);
        return ColorMap.getConcreteColor(colorString);
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
    
    public void messageAdmins(Component message) {
        Bukkit.getConsoleSender().sendMessage(message);
        for (Player admin : onlineAdmins) {
            admin.sendMessage(message);
        }
    }
    
    public void playSoundForAdmins(@NotNull String sound, float volume, float pitch) {
        for (Player admin : onlineAdmins) {
            admin.playSound(admin.getLocation(), sound, volume, pitch);
        }
    }
    
    public void messageOnlineParticipants(Component message) {
        for (Player participant : onlineParticipants) {
            participant.sendMessage(message);
        }
    }
    
    // Test methods
    
    public void setGameStateStorageUtil(GameStateStorageUtil gameStateStorageUtil) {
        this.gameStateStorageUtil = gameStateStorageUtil;
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
    
    public void setSidebarFactory(SidebarFactory sidebarFactory) {
        this.sidebarFactory = sidebarFactory;
        hubManager.initializeSidebar(sidebarFactory);
    }
    
    private void updateTeamScore(String team) {
        String displayName = getTeamDisplayName(team);
        ChatColor teamChatColor = getTeamChatColor(team);
        int teamScore = getScore(team);
        if (activeGame != null && activeGame instanceof Headerable headerable) {
            for (Player participant : getOnlinePlayersOnTeam(team)) {
                headerable.updateTeamScore(participant, String.format("%s%s: %s", teamChatColor, displayName, teamScore));
            }
        }
        if (eventManager.eventIsActive()) {
            eventManager.updateTeamScores();
        }
    }
    
    private void updatePersonalScore(Player participant) {
        int score = getScore(participant.getUniqueId());
        String contents = String.format("%sPoints: %s", ChatColor.GOLD, score);
        if (activeGame != null && activeGame instanceof Headerable headerable) {
            headerable.updatePersonalScore(participant, contents);
        }
        if (eventManager.eventIsActive()) {
            eventManager.updatePersonalScore(participant, contents);
        }
    }
    
}
