package org.braekpo1nt.mctmanager.games.gamemanager.states;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.commands.manager.commandresult.CommandResult;
import org.braekpo1nt.mctmanager.commands.manager.commandresult.CompositeCommandResult;
import org.braekpo1nt.mctmanager.config.exceptions.ConfigIOException;
import org.braekpo1nt.mctmanager.config.exceptions.ConfigInvalidException;
import org.braekpo1nt.mctmanager.games.gamemanager.GameManager;
import org.braekpo1nt.mctmanager.games.game.capturetheflag.CaptureTheFlagGame;
import org.braekpo1nt.mctmanager.games.game.capturetheflag.config.CaptureTheFlagConfig;
import org.braekpo1nt.mctmanager.games.game.capturetheflag.config.CaptureTheFlagConfigController;
import org.braekpo1nt.mctmanager.games.game.clockwork.ClockworkGame;
import org.braekpo1nt.mctmanager.games.game.clockwork.config.ClockworkConfig;
import org.braekpo1nt.mctmanager.games.game.clockwork.config.ClockworkConfigController;
import org.braekpo1nt.mctmanager.games.game.colossalcombat.ColossalCombatGame;
import org.braekpo1nt.mctmanager.games.game.colossalcombat.config.ColossalCombatConfig;
import org.braekpo1nt.mctmanager.games.game.colossalcombat.config.ColossalCombatConfigController;
import org.braekpo1nt.mctmanager.games.game.enums.GameType;
import org.braekpo1nt.mctmanager.games.game.example.ExampleGame;
import org.braekpo1nt.mctmanager.games.game.example.config.ExampleConfig;
import org.braekpo1nt.mctmanager.games.game.example.config.ExampleConfigController;
import org.braekpo1nt.mctmanager.games.game.farmrush.FarmRushGame;
import org.braekpo1nt.mctmanager.games.game.farmrush.config.FarmRushConfig;
import org.braekpo1nt.mctmanager.games.game.farmrush.config.FarmRushConfigController;
import org.braekpo1nt.mctmanager.games.game.footrace.FootRaceGame;
import org.braekpo1nt.mctmanager.games.game.footrace.config.FootRaceConfig;
import org.braekpo1nt.mctmanager.games.game.footrace.config.FootRaceConfigController;
import org.braekpo1nt.mctmanager.games.game.interfaces.MCTGame;
import org.braekpo1nt.mctmanager.games.game.parkourpathway.ParkourPathwayGame;
import org.braekpo1nt.mctmanager.games.game.parkourpathway.config.ParkourPathwayConfig;
import org.braekpo1nt.mctmanager.games.game.parkourpathway.config.ParkourPathwayConfigController;
import org.braekpo1nt.mctmanager.games.game.spleef.SpleefGame;
import org.braekpo1nt.mctmanager.games.game.spleef.config.SpleefConfig;
import org.braekpo1nt.mctmanager.games.game.spleef.config.SpleefConfigController;
import org.braekpo1nt.mctmanager.games.game.survivalgames.SurvivalGamesGame;
import org.braekpo1nt.mctmanager.games.game.survivalgames.config.SurvivalGamesConfig;
import org.braekpo1nt.mctmanager.games.game.survivalgames.config.SurvivalGamesConfigController;
import org.braekpo1nt.mctmanager.games.gamemanager.MCTParticipant;
import org.braekpo1nt.mctmanager.games.gamemanager.MCTTeam;
import org.braekpo1nt.mctmanager.games.gamestate.GameStateStorageUtil;
import org.braekpo1nt.mctmanager.games.utils.GameManagerUtils;
import org.braekpo1nt.mctmanager.games.utils.ParticipantInitializer;
import org.braekpo1nt.mctmanager.hub.config.HubConfig;
import org.braekpo1nt.mctmanager.hub.leaderboard.LeaderboardManager;
import org.braekpo1nt.mctmanager.participant.ColorAttributes;
import org.braekpo1nt.mctmanager.participant.OfflineParticipant;
import org.braekpo1nt.mctmanager.participant.Participant;
import org.braekpo1nt.mctmanager.participant.Team;
import org.braekpo1nt.mctmanager.ui.sidebar.KeyLine;
import org.braekpo1nt.mctmanager.ui.sidebar.Sidebar;
import org.braekpo1nt.mctmanager.ui.sidebar.SidebarFactory;
import org.braekpo1nt.mctmanager.ui.tablist.TabList;
import org.braekpo1nt.mctmanager.utils.ColorMap;
import org.braekpo1nt.mctmanager.utils.LogType;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.scoreboard.Scoreboard;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;

public abstract class GameManagerState {
    
    protected final @NotNull GameManager context;
    protected final @NotNull ContextReference contextReference;
    protected final @NotNull TabList tabList;
    protected final Scoreboard mctScoreboard;
    protected final @NotNull Map<GameType, MCTGame> activeGames;
    
    protected final Map<String, MCTTeam> teams;
    protected final Map<UUID, OfflineParticipant> allParticipants;
    protected final Map<UUID, MCTParticipant> onlineParticipants;
    protected final List<Player> onlineAdmins;
    protected final Main plugin;
    protected final GameStateStorageUtil gameStateStorageUtil;
    protected final SidebarFactory sidebarFactory;
    protected final HubConfig config;
    protected final List<LeaderboardManager> leaderboardManagers;
    protected final Sidebar sidebar;
    /**
     * A reference to which participant is in which game<br>
     * If a participant's UUID is a key in this map, that participant is
     * in a game.
     */
    protected final Map<UUID, GameType> participantGames;
    /**
     * A reference to which admin is in which game<br>
     * If an admin's UUID is a key in this map, that admin is in 
     * a game.
     */
    protected final Map<UUID, GameType> adminGames;
    
    public GameManagerState(
            @NotNull GameManager context,
            @NotNull ContextReference contextReference) {
        this.context = context;
        this.contextReference = contextReference;
        this.tabList = contextReference.getTabList();
        this.leaderboardManagers = contextReference.getLeaderboardManagers();
        this.sidebar = contextReference.getSidebar();
        this.mctScoreboard = contextReference.getMctScoreboard();
        this.participantGames = contextReference.getParticipantGames();
        this.adminGames = contextReference.getAdminGames();
        this.plugin = contextReference.getPlugin();
        this.activeGames = contextReference.getActiveGames();
        this.gameStateStorageUtil = contextReference.getGameStateStorageUtil();
        this.sidebarFactory = contextReference.getSidebarFactory();
        
        this.teams = contextReference.getTeams();
        this.allParticipants = contextReference.getAllParticipants();
        this.onlineParticipants = contextReference.getOnlineParticipants();
        this.onlineAdmins = contextReference.getOnlineAdmins();
        this.config = contextReference.getConfig();
    }
    
    public abstract CommandResult switchMode(@NotNull String mode);
    
    public void cleanup() {
        this.leaderboardManagers.forEach(LeaderboardManager::tearDown);
        this.tabList.cleanup();
        this.sidebar.deleteAllLines();
        this.onlineAdmins.clear();
        this.onlineParticipants.clear();
        this.teams.clear();
        this.allParticipants.clear();
    }
    
    // leave/join start
    public void onAdminJoin(@NotNull PlayerJoinEvent event, @NotNull Player admin) {
        onAdminJoin(admin);
        event.joinMessage(GameManagerUtils.replaceWithDisplayName(admin, event.joinMessage()));
    }
    
    public void onAdminJoin(@NotNull Player admin) {
        context.getOnlineAdmins().add(admin);
        admin.setScoreboard(context.getMctScoreboard());
        admin.addPotionEffect(Main.NIGHT_VISION);
        Component displayName = Component.empty()
                .append(Component.text("[Admin]")
                        .color(GameManager.ADMIN_COLOR))
                .append(Component.text(admin.getName()));
        admin.displayName(displayName);
        admin.playerListName(displayName);
        tabList.showPlayer(admin);
        sidebar.addPlayer(admin);
        updateSidebarTeamScores();
    }
    
    public void onParticipantJoin(@NotNull PlayerJoinEvent event, @NotNull MCTParticipant participant) {
        onParticipantJoin(participant);
        event.joinMessage(GameManagerUtils.replaceWithDisplayName(participant, event.joinMessage()));
    }
    
    /**
     * Handles when a participant joins
     * @param participant the participant who joined
     */
    public void onParticipantJoin(@NotNull MCTParticipant participant) {
        onlineParticipants.put(participant.getUniqueId(), participant);
        MCTTeam team = teams.get(participant.getTeamId());
        team.joinOnlineMember(participant);
        setupScoreboard(participant);
        participant.addPotionEffect(Main.NIGHT_VISION);
        Component displayName = Component.text(participant.getName(), team.getColor());
        participant.getPlayer().displayName(displayName);
        participant.getPlayer().playerListName(displayName);
        tabList.showPlayer(participant);
        tabList.setParticipantGrey(participant.getParticipantID(), false);
        sidebar.addPlayer(participant);
        ColorMap.colorLeatherArmor(participant, team.getBukkitColor());
        leaderboardManagers.forEach(manager -> manager.showPlayer(participant.getPlayer()));
        updateScoreVisuals(Collections.singletonList(team), Collections.singletonList(participant));
    }
    
    /**
     * Set up the internal scoreboard connections
     * @param participant the participant to set up the scoreboard for
     */
    private void setupScoreboard(@NotNull MCTParticipant participant) {
        participant.getPlayer().setScoreboard(mctScoreboard);
        org.bukkit.scoreboard.Team scoreboardTeam = mctScoreboard.getTeam(participant.getTeamId());
        if (scoreboardTeam != null) {
            scoreboardTeam.addPlayer(participant.getPlayer());
        } else {
            Main.logger().severe(String.format("Error retrieving team with ID %s from scoreboard", participant.getTeamId()));
        }
    }
    
    public void onAdminQuit(@NotNull PlayerQuitEvent event, @NotNull Player admin) {
        event.quitMessage(GameManagerUtils.replaceWithDisplayName(admin, event.quitMessage()));
        onAdminQuit(admin);
    }
    
    public void onAdminQuit(@NotNull Player admin) {
        onlineAdmins.remove(admin);
        tabList.hidePlayer(admin.getUniqueId());
        sidebar.removePlayer(admin);
        Component displayName = Component.text(admin.getName(), NamedTextColor.WHITE);
        admin.displayName(displayName);
        admin.playerListName(displayName);
    }
    
    public void onParticipantQuit(@NotNull PlayerQuitEvent event, @NotNull MCTParticipant participant) {
        event.quitMessage(GameManagerUtils.replaceWithDisplayName(participant, event.quitMessage()));
        onParticipantQuit(participant);
    }
    
    /**
     * Handles when a participant leaves the event.
     * Should be called when a participant disconnects (quits/leaves) from the server 
     * (see {@link GameManager#onPlayerQuit(PlayerQuitEvent)}),
     * or when they are removed from the participants list
     * @param participant The participant who left the event
     * @see GameManager#leaveParticipant(OfflineParticipant)
     */
    public void onParticipantQuit(@NotNull MCTParticipant participant) {
        GameType gameType = participantGames.get(participant.getUniqueId());
        if (gameType != null) {
            MCTGame activeGame = activeGames.get(gameType);
            if (activeGame != null) {
                activeGame.onParticipantQuit(participant.getUniqueId());
                activeGame.onTeamQuit(participant.getTeamId());
            }
            participantGames.remove(participant.getUniqueId());
            tabList.showPlayer(participant);
            sidebar.addPlayer(participant);
        }
        MCTTeam team = teams.get(participant.getTeamId());
        team.quitOnlineMember(participant.getUniqueId());
        onlineParticipants.remove(participant.getUniqueId());
        tabList.hidePlayer(participant.getUniqueId());
        sidebar.removePlayer(participant);
        Component displayName = Component.text(participant.getName(),
                NamedTextColor.WHITE);
        participant.getPlayer().displayName(displayName);
        participant.getPlayer().playerListName(displayName);
        GameManagerUtils.deColorLeatherArmor(participant.getInventory());
        tabList.setParticipantGrey(participant.getParticipantID(), true);
        leaderboardManagers.forEach(manager -> manager.hidePlayer(participant.getPlayer()));
    }
    // leave/join end
    
    // ui start
    public void updateScoreVisuals(Collection<MCTTeam> mctTeams, Collection<MCTParticipant> mctParticipants) {
        if (!mctTeams.isEmpty()) {
            updateSidebarTeamScores();
        }
        updateSidebarPersonalScores(mctParticipants);
        tabList.setScores(mctTeams);
        context.updateLeaderboards();
    }
    
    /**
     * @return a new sidebar
     */
    public Sidebar createSidebar() {
        return sidebarFactory.createSidebar();
    }
    
    /**
     * Reorders the team lines in the sidebar from highest to lowest
     * score, up to 10
     */
    public void updateSidebarTeamScores() {
        List<Team> sortedTeams = context.getSortedTeams();
        int numOfTeamLines = Math.min(10, sortedTeams.size());
        KeyLine[] teamLines = new KeyLine[10];
        for (int i = 0; i < numOfTeamLines; i++) {
            Team team = sortedTeams.get(i);
            teamLines[i] = new KeyLine("team"+i, Component.empty()
                    .append(team.getFormattedDisplayName())
                    .append(Component.text(": "))
                    .append(Component.text(team.getScore())
                            .color(NamedTextColor.GOLD))
            );
        }
        // fill out any empty lines
        for (int i = numOfTeamLines; i < 10; i++) {
            teamLines[i] = new KeyLine("team"+i, Component.empty());
        }
        sidebar.updateLines(teamLines);
    }
    
    /**
     * Updates the sidebars of the given participants, unless they are in a game
     * @param mctParticipants the participants to update the sidebars of
     */
    public void updateSidebarPersonalScores(Collection<MCTParticipant> mctParticipants) {
        for (MCTParticipant participant : mctParticipants) {
            if (!isParticipantInGame(participant)) {
                sidebar.updateLine(participant.getUniqueId(), "personalScore",
                        Component.empty()
                                .append(Component.text("Personal: "))
                                .append(Component.text(participant.getScore()))
                                .color(NamedTextColor.GOLD));
            }
        }
    }
    
    protected void displayStats(Map<String, Integer> teamScores, Map<UUID, Integer> participantScores) {
        List<MCTTeam> sortedTeams = teamScores.keySet().stream()
                .map(teams::get)
                .filter(t -> teamScores.containsKey(t.getTeamId()))
                .sorted(Comparator.comparing(
                        t -> teamScores.get(t.getTeamId()),
                        Comparator.reverseOrder()))
                .toList();
        List<OfflineParticipant> sortedParticipants = participantScores.keySet().stream()
                .map(allParticipants::get)
                .filter(p -> participantScores.containsKey(p.getUniqueId()))
                .sorted(Comparator.comparing(
                        p -> participantScores.get(p.getUniqueId()),
                        Comparator.reverseOrder()))
                .toList();
        
        TextComponent.Builder everyone = Component.text();
        everyone.append(
                Component.empty()
                        .append(Component.text("["))
                        .append(Component.text(getMultiplier()))
                        .append(Component.text(" Multiplier]\n"))
                        .color(NamedTextColor.GRAY)
        );
        everyone.append(Component.text("Top 5 Teams:"))
                .append(Component.newline());
        for (int i = 0; i < Math.min(sortedTeams.size(), 5); i++) {
            MCTTeam team = sortedTeams.get(i);
            everyone
                    .append(Component.text("  "))
                    .append(Component.text(i+1))
                    .append(Component.text(". "))
                    .append(team.getFormattedDisplayName())
                    .append(Component.text(": "))
                    .append(Component.text(teamScores.get(team.getTeamId()))
                            .color(NamedTextColor.GOLD))
                    .append(Component.newline());
        }
        everyone.append(Component.text("\nTop 5 Participants:"))
                .append(Component.newline());
        for (int i = 0; i < Math.min(sortedParticipants.size(), 5); i++) {
            OfflineParticipant participant = sortedParticipants.get(i);
            everyone
                    .append(Component.text("  "))
                    .append(Component.text(i+1))
                    .append(Component.text(". "))
                    .append(participant.displayName())
                    .append(Component.text(": "))
                    .append(Component.text(participantScores.get(participant.getUniqueId()))
                            .color(NamedTextColor.GOLD))
                    .append(Component.text(" ("))
                    .append(Component.text((int) (participantScores.get(participant.getUniqueId()) / getMultiplier()))
                            .color(NamedTextColor.GOLD))
                    .append(Component.text(" x "))
                    .append(Component.text(getMultiplier()))
                    .append(Component.text(")"))
                    .append(Component.newline());
        }
        Audience.audience(
                Audience.audience(sortedTeams),
                plugin.getServer().getConsoleSender()
        ).sendMessage(everyone.build());
        
        for (MCTTeam team : sortedTeams) {
            TextComponent.Builder message = Component.text();
            message
                    .append(team.getFormattedDisplayName())
                    .append(Component.text(": "))
                    .append(Component.text(teamScores.get(team.getTeamId()))
                            .color(NamedTextColor.GOLD))
                    .append(Component.newline());
            int i = 1;
            for (OfflineParticipant participant : sortedParticipants) {
                if (participant.getTeamId().equals(team.getTeamId())) {
                    message
                            .append(Component.text("  "))
                            .append(Component.text(i))
                            .append(Component.text(". "))
                            .append(participant.displayName())
                            .append(Component.text(": "))
                            .append(Component.text(participantScores.get(participant.getUniqueId()))
                                    .color(NamedTextColor.GOLD))
                            .append(Component.text(" ("))
                            .append(Component.text((int) (participantScores.get(participant.getUniqueId()) / getMultiplier()))
                                    .color(NamedTextColor.GOLD))
                            .append(Component.text(" x "))
                            .append(Component.text(getMultiplier()))
                            .append(Component.text(")"))
                            .append(Component.newline());
                    i++;
                }
            }
            team.sendMessage(message.build());
        }
        
        for (OfflineParticipant offlineParticipant : sortedParticipants) {
            Participant participant = onlineParticipants.get(offlineParticipant.getUniqueId());
            if (participant != null) {
                participant.sendMessage(
                        Component.empty()
                                .append(Component.text("Personal")
                                        .color(NamedTextColor.GOLD))
                                .append(Component.text(": "))
                                .append(Component.text(participantScores.get(offlineParticipant.getUniqueId()))
                                        .color(NamedTextColor.GOLD))
                                .append(Component.text(" ("))
                                .append(Component.text((int) (participantScores.get(offlineParticipant.getUniqueId()) / getMultiplier()))
                                        .color(NamedTextColor.GOLD))
                                .append(Component.text(" x "))
                                .append(Component.text(getMultiplier()))
                                .append(Component.text(")"))
                );
            }
        }
    }
    // ui end
    
    // game start
    public CommandResult startGame(@NotNull Set<String> teamIds, @NotNull List<Player> gameAdmins, @NotNull GameType gameType, @NotNull String configFile) {
        if (teamIds.isEmpty()) {
            return CommandResult.failure("Can't start a game with no teams.");
        }
        
        if (activeGames.containsKey(gameType)) {
            return CommandResult.failure(Component.text("There is already a ")
                    .append(Component.text(gameType.getTitle()))
                    .append(Component.text(" game running.")));
        }
        
        if (context.editorIsRunning()) {
            return CommandResult.failure(Component.text("There is an editor running. You must stop the editor before you start a game."));
        }
        
        if (onlineParticipants.isEmpty()) {
            return CommandResult.failure(Component.text("There are no online participants. You can add participants using:\n")
                    .append(Component.text("/mct team join <team> <member>")
                            .decorate(TextDecoration.BOLD)
                            .clickEvent(ClickEvent.suggestCommand("/mct team join "))));
        }
        
        Set<MCTTeam> gameTeams = new HashSet<>();
        Set<MCTParticipant> gameParticipants = new HashSet<>();
        for (String teamId : teamIds) {
            MCTTeam team = teams.get(teamId);
            if (team == null) {
                return CommandResult.failure(Component.empty()
                        .append(Component.text(teamId)
                                .decorate(TextDecoration.BOLD))
                        .append(Component.text(" is not a valid teamId")));
            }
            Collection<MCTParticipant> onlineMembers = team.getOnlineMembers();
            for (MCTParticipant onlineMember : onlineMembers) {
                if (participantGames.containsKey(onlineMember.getUniqueId())) {
                    return CommandResult.failure(Component.empty()
                            .append(Component.text("Can't start a game with "))
                            .append(team.getFormattedDisplayName())
                            .append(Component.text(" because one of its members is already in a game")));
                }
            }
            if (!onlineMembers.isEmpty()) {
                gameTeams.add(team);
                gameParticipants.addAll(onlineMembers);
            }
        }
        // make sure the player and team count requirements are met
        if (gameTeams.isEmpty()) {
            return CommandResult.failure("None of the specified teams are online");
        }
        switch (gameType) {
            case CAPTURE_THE_FLAG -> {
                if (gameTeams.size() < 2) {
                    return CommandResult.failure(Component.text("Capture the Flag needs at least 2 teams online to play.").color(NamedTextColor.RED));
                }
            }
            case FINAL -> {
                if (gameTeams.size() < 2) {
                    return CommandResult.failure(Component.empty()
                            .append(Component.text(GameType.FINAL.getTitle()))
                            .append(Component.text(" needs at least 2 teams online to play")));
                }
            }
        }
        
        Component title = createNewTitle(gameType.getTitle());
        for (MCTParticipant participant : gameParticipants) {
            onParticipantJoinGame(gameType, participant);
        }
        for (Player admin : gameAdmins) {
            onAdminJoinGame(gameType, admin);
        }
        
        try {
            activeGames.put(gameType,
                    instantiateGame(
                            gameType, 
                            title, 
                            configFile, 
                            new HashSet<>(gameTeams), 
                            new HashSet<>(gameParticipants), 
                            gameAdmins));
        } catch (Exception e) {
            for (MCTParticipant participant : gameParticipants) {
                participantGames.remove(participant.getUniqueId());
                tabList.showPlayer(participant);
                sidebar.addPlayer(participant);
            }
            for (Player admin : gameAdmins) {
                adminGames.remove(admin.getUniqueId());
                tabList.showPlayer(admin);
                sidebar.addPlayer(admin);
            }
            Main.logger().log(Level.SEVERE, String.format("Error starting game %s", gameType), e);
            return CommandResult.failure(Component.text("Can't start ")
                    .append(Component.text(gameType.name())
                            .decorate(TextDecoration.BOLD))
                    .append(Component.text(". Error starting game. See console for details:\n"))
                    .append(Component.text(e.getMessage()))
                    .color(NamedTextColor.RED));
        }
        return CommandResult.success();
    }
    
    protected void onParticipantJoinGame(@NotNull GameType gameType, MCTParticipant participant) {
        participantGames.put(participant.getUniqueId(), gameType);
        tabList.hidePlayer(participant);
        sidebar.removePlayer(participant);
    }
    
    protected void onAdminJoinGame(@NotNull GameType gameType, Player admin) {
        adminGames.put(admin.getUniqueId(), gameType);
        tabList.hidePlayer(admin);
        sidebar.removePlayer(admin);
    }
    
    public CommandResult stopGame(@NotNull GameType gameType) {
        MCTGame game = activeGames.get(gameType);
        if (game == null) {
            return CommandResult.failure(Component.empty()
                    .append(Component.text("No instances of game "))
                    .append(Component.text(gameType.getTitle())
                            .decorate(TextDecoration.BOLD))
                    .append(Component.text(" are active")));
        }
        game.stop();
        return CommandResult.success();
    }
    
    public CommandResult stopAllGames() {
        if (activeGames.isEmpty()) {
            return CommandResult.success(Component.text("No games are running"));
        }
        List<MCTGame> gamesToCancel = new ArrayList<>(activeGames.values());
        List<CommandResult> results = new ArrayList<>(activeGames.size());
        for (MCTGame game : gamesToCancel) {
            game.stop();
            results.add(CommandResult.success(Component.empty()
                    .append(Component.text("Stopped "))
                    .append(Component.text(game.getType().getTitle()))));
        }
        return CompositeCommandResult.all(results);
    }
    
    /**
     * Called by an active game when the game is over.
     *
     * @param gameType          the type of game that ended
     * @param teamScores        the team scores
     * @param participantScores the participant scores
     * @param gameParticipants  the UUIDs of the participants which are online and were in the finished
     *                          game. Must be UUIDs which are keys in {@link #onlineParticipants}.
     * @param gameAdmins        the admins who were in the game
     */
    public void gameIsOver(@NotNull GameType gameType, Map<String, Integer> teamScores, Map<UUID, Integer> participantScores, @NotNull Collection<UUID> gameParticipants, @NotNull List<Player> gameAdmins) {
        MCTGame game = activeGames.remove(gameType);
        if (game == null) {
            return;
        }
        for (UUID uuid : gameParticipants) {
            MCTParticipant participant = onlineParticipants.get(uuid);
            onParticipantReturnToHub(participant, config.getSpawn());
            participant.sendMessage(Component.text("Returning to hub"));
        }
        for (Player admin : gameAdmins) {
            onAdminReturnToHub(admin, config.getSpawn());
            admin.sendMessage(Component.text("Returning to hub"));
        }
        addScores(teamScores, participantScores, gameType);
    }
    
    /**
     * Add the given scores to the given teams and participants, save the game state, update
     * the UI, etc. <br>
     * If any invalid teamIds or UUIDs are used, there will be errors 
     *
     * @param newTeamScores        map of teamId to score to add. Must be teamIds of real teams
     * @param newParticipantScores map of UUID to score to add. Must be UUIDs of real participants
     * @param gameType             the type of the game
     */
    protected void addScores(Map<String, Integer> newTeamScores, Map<UUID, Integer> newParticipantScores, GameType gameType) {
        // some values might be from offline teams who have been removed, but still saved as QuitData
        Map<String, Integer> teamScores = newTeamScores.entrySet().stream()
                .filter(e -> teams.containsKey(e.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        Map<UUID, Integer> participantScores = newParticipantScores.entrySet().stream()
                .filter(e -> allParticipants.containsKey(e.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        for (Map.Entry<String, Integer> entry : teamScores.entrySet()) {
            String teamId = entry.getKey();
            int newScore = entry.getValue();
            MCTTeam team = teams.get(teamId);
            teams.put(teamId, new MCTTeam(team, team.getScore() + newScore));
        }
        for (Map.Entry<UUID, Integer> entry : participantScores.entrySet()) {
            UUID uuid = entry.getKey();
            int newScore = entry.getValue();
            OfflineParticipant offlineParticipant = allParticipants.get(uuid);
            allParticipants.put(uuid, new OfflineParticipant(offlineParticipant,
                    offlineParticipant.getScore() + newScore));
            MCTParticipant participant = onlineParticipants.get(uuid);
            if (participant != null) {
                onlineParticipants.put(uuid, new MCTParticipant(participant,
                        participant.getScore() + newScore));
            }
        }
        try {
            gameStateStorageUtil.updateScores(teams.values(), allParticipants.values());
            if (plugin.isEnabled()) {
                plugin.getServer().getScheduler().runTaskAsynchronously(plugin, gameStateStorageUtil::saveGameState);
            } else {
                gameStateStorageUtil.saveGameState();
            }
        } catch (ConfigIOException e) {
            context.reportGameStateException("updating scores", e);
        }
        updateScoreVisuals(teams.values(), onlineParticipants.values());
        displayStats(teamScores, participantScores);
    }
    
    protected void onParticipantReturnToHub(@NotNull MCTParticipant participant, @NotNull Location spawn) {
        participantGames.remove(participant.getUniqueId());
        participant.setGameMode(GameMode.ADVENTURE);
        ParticipantInitializer.clearInventory(participant);
        ParticipantInitializer.resetHealthAndHunger(participant);
        ParticipantInitializer.clearStatusEffects(participant);
        participant.teleport(spawn);
        tabList.showPlayer(participant);
        sidebar.addPlayer(participant);
        MCTTeam team = teams.get(participant.getTeamId());
        updateScoreVisuals(Collections.singletonList(team), Collections.singletonList(participant));
    }
    
    protected void onAdminReturnToHub(@NotNull Player admin, @NotNull Location spawn) {
        adminGames.remove(admin.getUniqueId());
        admin.setGameMode(GameMode.SPECTATOR);
        admin.teleport(spawn);
        tabList.showPlayer(admin);
        sidebar.addPlayer(admin);
        updateSidebarTeamScores();
    }
    
    /**
     * @param gameType the {@link GameType} to instantiate the {@link MCTGame} for
     * @return a new {@link MCTGame} instance for the given type. Null if the given type is null. 
     */
    protected MCTGame instantiateGame(
            @NotNull GameType gameType,
            Component title,
            String configFile,
            Collection<Team> newTeams,
            Collection<Participant> newParticipants,
            List<Player> newAdmins) throws ConfigIOException, ConfigInvalidException {
        return switch (gameType) {
            case SPLEEF -> {
                SpleefConfig config = new SpleefConfigController(plugin.getDataFolder(), gameType.getId()).getConfig(configFile);
                yield new SpleefGame(plugin, context, title, config, newTeams, newParticipants, newAdmins);
            }
            case CLOCKWORK -> {
                ClockworkConfig config = new ClockworkConfigController(plugin.getDataFolder(), gameType.getId()).getConfig(configFile);
                yield new ClockworkGame(plugin, context, title, config, newTeams, newParticipants, newAdmins);
            }
            case SURVIVAL_GAMES -> {
                SurvivalGamesConfig config = new SurvivalGamesConfigController(plugin.getDataFolder(), gameType.getId()).getConfig(configFile);
                yield new SurvivalGamesGame(plugin, context, title, config, newTeams, newParticipants, newAdmins);
            }
            case FARM_RUSH -> {
                FarmRushConfig config = new FarmRushConfigController(plugin.getDataFolder(), gameType.getId()).getConfig(configFile);
                yield new FarmRushGame(plugin, context, title, config, newTeams, newParticipants, newAdmins);
            }
            case FOOT_RACE -> {
                FootRaceConfig config = new FootRaceConfigController(plugin.getDataFolder(), gameType.getId()).getConfig(configFile);
                yield new FootRaceGame(plugin, context, title, config, newTeams, newParticipants, newAdmins);
            }
            case PARKOUR_PATHWAY -> {
                ParkourPathwayConfig config = new ParkourPathwayConfigController(plugin.getDataFolder(), gameType.getId()).getConfig(configFile);
                yield new ParkourPathwayGame(plugin, context, title, config, newTeams, newParticipants, newAdmins);
            }
            case CAPTURE_THE_FLAG -> {
                CaptureTheFlagConfig config = new CaptureTheFlagConfigController(plugin.getDataFolder(), gameType.getId()).getConfig(configFile);
                yield new CaptureTheFlagGame(plugin, context, title, config, newTeams, newParticipants, newAdmins);
            }
            case EXAMPLE -> {
                ExampleConfig config = new ExampleConfigController(plugin.getDataFolder(), gameType.getId()).getConfig(configFile);
                yield new ExampleGame(plugin, context, title, config, newTeams, newParticipants, newAdmins);
            }
            case FINAL -> {
                ColossalCombatConfig config = new ColossalCombatConfigController(plugin.getDataFolder(), gameType.getId()).getConfig(configFile);
                List<Team> sortedTeams = newTeams.stream()
                        .sorted((t1, t2) -> {
                            int scoreComparison = t2.getScore() - t1.getScore();
                            if (scoreComparison != 0) {
                                return scoreComparison;
                            }
                            return t1.getDisplayName().compareToIgnoreCase(t2.getDisplayName());
                        }).toList();
                yield new ColossalCombatGame(plugin, context, title, config, sortedTeams.getFirst(), sortedTeams.get(1), sortedTeams, newParticipants, newAdmins);
            }
        };
    }
    
    protected @NotNull Component createNewTitle(String baseTitle) {
        return Component.empty()
                .append(Component.text(baseTitle))
                .color(NamedTextColor.BLUE);
    }
    // game stop
    
    // event start
    public CommandResult startEvent(int maxGames, int currentGameNumber) {
        return CommandResult.failure("Can't start an event in this mode");
    }
    
    public CommandResult stopEvent() {
        return CommandResult.failure("No event is running");
    }
    
    public boolean eventIsActive() {
        return false;
    }
    
    public CommandResult readyUpParticipant(@NotNull MCTParticipant participant) {
        return CommandResult.failure("Can't ready up at this time");
    }
    
    public CommandResult unReadyParticipant(@NotNull MCTParticipant participant) {
        return CommandResult.failure("Can't un-ready at this time");
    }
    
    public CommandResult openHubMenu(@NotNull MCTParticipant participant) {
        return CommandResult.failure("Can't open the hub menu at this time");
    }
    
    public int getGameIterations(@NotNull GameType gameType) {
        return -1;
    }
    
    public CommandResult undoGame(@NotNull GameType gameType, int iterationIndex) {
        return CommandResult.failure("Can't undo games in this state");
    }
    
    public CommandResult modifyMaxGames(int newMaxGames) {
        return CommandResult.failure("No event is active");
    }
    
    public CommandResult addGameToVotingPool(@NotNull GameType gameToAdd) {
        return CommandResult.failure("No event is active");
    }
    
    public CommandResult removeGameFromVotingPool(@NotNull GameType gameToRemove) {
        return CommandResult.failure("No event is active");
    }
    
    public CommandResult listReady(@Nullable String teamId) {
        return CommandResult.failure("No event is active");
    }
    
    public void setWinner(@NotNull MCTTeam team) {
        // do nothing
    }
    // event stop
    
    // team start
    /**
     * Add a team to the game.
     * @param teamId The teamId of the team. If a team with the given id already exists, nothing happens.
     * @param teamDisplayName The display name of the team.
     * @param colorString the string representing the color
     * @return the newly created team, or null if the given team already exists or could not be created
     */
    public Team addTeam(String teamId, String teamDisplayName, String colorString) {
        if (teams.containsKey(teamId)) {
            return null;
        }
        try {
            gameStateStorageUtil.addTeam(teamId, teamDisplayName, colorString);
        } catch (ConfigIOException e) {
            context.reportGameStateException("adding a team", e);
        }
        
        NamedTextColor color = ColorMap.getNamedTextColor(colorString);
        ColorAttributes colorAttributes = ColorMap.getColorAttributes(colorString);
        MCTTeam team = new MCTTeam(teamId, teamDisplayName, color, colorAttributes, 0);
        teams.put(teamId, team);
        
        org.bukkit.scoreboard.Team newTeam = mctScoreboard.registerNewTeam(teamId);
        newTeam.displayName(Component.text(teamDisplayName));
        newTeam.color(color);
        tabList.addTeam(teamId, teamDisplayName, color);
        updateScoreVisuals(Collections.singletonList(team), Collections.emptyList());
        return team;
    }
    
    /**
     * Remove the given team from the game
     * @param teamId teamId of the team to remove
     */
    public CommandResult removeTeam(String teamId) {
        MCTTeam team = teams.get(teamId);
        if (team == null) {
            return CommandResult.failure(Component.text("Team ")
                    .append(Component.text(teamId))
                    .append(Component.text(" does not exist.")));
        }
        Set<OfflineParticipant> members = team.getMemberUUIDs().stream().map(allParticipants::get).collect(Collectors.toSet());
        List<CommandResult> results = new ArrayList<>();
        for (OfflineParticipant member : members) {
            results.add(leaveParticipant(member));
        }
        teams.remove(team.getTeamId());
        tabList.removeTeam(teamId);
        updateSidebarTeamScores();
        try {
            gameStateStorageUtil.removeTeam(teamId);
            results.add(CommandResult.success(Component.text("Removed team ")
                    .append(team.getFormattedDisplayName())));
        } catch (ConfigIOException e) {
            context.reportGameStateException("removing team", e);
            results.add(CommandResult.failure(Component.text("error occurred removing team, see console for details.")));
            return CompositeCommandResult.all(results);
        }
        org.bukkit.scoreboard.Team scoreboardTeam = mctScoreboard.getTeam(teamId);
        if (scoreboardTeam != null) {
            scoreboardTeam.unregister();
        } else {
            Main.logger().warning(String.format("mctScoreboard could not find team \"%s\" (removeTeam)", teamId));
        }
        
        return CompositeCommandResult.all(results);
    }
    // team stop
    
    // participant start
    /**
     * Joins the given player to the team with the given teamId. If the player was on a team already (not teamId) they will be removed from that team and added to the other team. 
     * Note, this will not join a player to a team if that player is an admin. 
     * @param offlinePlayer The player to join to the given team
     * @param name The name of the participant to join to the given team
     * @param teamId The internal teamId of the team to join the player to.
     */
    public CommandResult joinParticipantToTeam(@NotNull OfflinePlayer offlinePlayer, @NotNull String name, @NotNull String teamId) {
        MCTTeam team = teams.get(teamId);
        if (team == null) {
            return CommandResult.failure(Component.empty()
                    .append(Component.text(teamId).decorate(TextDecoration.BOLD))
                    .append(Component.text(" is not a valid teamId")));
        }
        List<CommandResult> results = new ArrayList<>();
        if (context.isAdmin(offlinePlayer.getUniqueId())) {
            results.add(removeAdmin(offlinePlayer, name));
        }
        OfflineParticipant existingParticipant = allParticipants.get(offlinePlayer.getUniqueId());
        if (existingParticipant != null) {
            if (existingParticipant.getTeamId().equals(teamId)) {
                results.add(CommandResult.success(Component.empty()
                        .append(existingParticipant.displayName())
                        .append(Component.text(" is already a member of "))
                        .append(team.getFormattedDisplayName())
                        .append(Component.text(". Nothing happened."))));
                return CompositeCommandResult.all(results);
            }
            results.add(leaveParticipant(existingParticipant));
        }
        
        org.bukkit.scoreboard.Team scoreboardTeam = mctScoreboard.getTeam(team.getTeamId());
        if (scoreboardTeam != null) {
            scoreboardTeam.addPlayer(offlinePlayer);
        } else {
            Main.logger().warning(String.format("mctScoreboard could not find team \"%s\" (joinParticipantToTeam)", team.getTeamId()));
        }
        
        Component displayName = team.createDisplayName(name);
        OfflineParticipant offlineParticipant = new OfflineParticipant(offlinePlayer.getUniqueId(), name, displayName, teamId, 0);
        allParticipants.put(offlineParticipant.getUniqueId(), offlineParticipant);
        team.joinMember(offlineParticipant.getUniqueId());
        tabList.joinParticipant(
                offlineParticipant.getParticipantID(),
                offlineParticipant.getName(),
                offlineParticipant.getTeamId(),
                true);
        try {
            gameStateStorageUtil.addNewPlayer(offlineParticipant.getUniqueId(), offlineParticipant.getName(), offlineParticipant.getTeamId());
        } catch (ConfigIOException e) {
            context.reportGameStateException("adding new player", e);
            results.add(CommandResult.failure(Component.text("error occurred adding new player, see console for details.")));
        }
        context.updateLeaderboards();
        results.add(CommandResult.success(Component.text("Joined ")
                .append(offlineParticipant.displayName())
                .append(Component.text(" to "))
                .append(team.getFormattedDisplayName())));
        
        // if they are online
        Player player = offlinePlayer.getPlayer();
        if (player != null) {
            MCTParticipant participant = new MCTParticipant(offlineParticipant, player);
            participant.sendMessage(Component.text("You've been joined to team ")
                    .append(team.getFormattedDisplayName()));
            onParticipantJoin(participant);
        }
        
        return CompositeCommandResult.all(results);
    }
    
    /**
     * Leaves the player from the team and removes them from the game state.
     * If a game is running, and the player is online, removes that player from the game as well. 
     * @param offlineParticipant The participant to remove from their team
     */
    public CommandResult leaveParticipant(@NotNull OfflineParticipant offlineParticipant) {
        MCTTeam team = teams.get(offlineParticipant.getTeamId());
        MCTParticipant participant = onlineParticipants.get(offlineParticipant.getUniqueId());
        if (participant != null) {
            onParticipantQuit(participant);
            participant.sendMessage(Component.text("You've been removed from ")
                    .append(team.getFormattedDisplayName()));
            onlineParticipants.remove(participant.getUniqueId());
        }
        team.leaveMember(offlineParticipant.getUniqueId());
        allParticipants.remove(offlineParticipant.getUniqueId());
        try {
            gameStateStorageUtil.leavePlayer(offlineParticipant.getUniqueId());
        } catch (ConfigIOException e) {
            context.reportGameStateException("leaving player", e);
            return CommandResult.failure(Component.text("error occurred leaving player, see console for details."));
        }
        context.updateLeaderboards();
        tabList.leaveParticipant(offlineParticipant.getParticipantID());
        org.bukkit.scoreboard.Team scoreboardTeam = mctScoreboard.getTeam(offlineParticipant.getTeamId());
        if (scoreboardTeam != null) {
            if (offlineParticipant.getPlayer() != null) {
                scoreboardTeam.removePlayer(offlineParticipant.getPlayer());
            }
        } else {
            Main.logger().warning(String.format("mctScoreboard could not find team \"%s\" (leaveParticipant)", offlineParticipant.getTeamId()));
        }
        return CommandResult.success(Component.text("Removed ")
                .append(offlineParticipant.displayName())
                .append(Component.text(" from team "))
                .append(team.getFormattedDisplayName()));
    }
    
    public CommandResult joinParticipantToGame(@NotNull GameType gameType, @NotNull MCTParticipant participant) {
        if (isParticipantInGame(participant)) {
            return CommandResult.failure(Component.text("Already in a game"));
        }
        MCTGame activeGame = activeGames.get(gameType);
        if (activeGame == null) {
            return CommandResult.failure(Component.empty()
                    .append(Component.text("No "))
                    .append(Component.text(gameType.getTitle()))
                    .append(Component.text(" game is active right now")));
        }
        GameType teamGameType = context.getTeamActiveGame(participant.getTeamId());
        if (teamGameType != null && !teamGameType.equals(gameType)) {
            MCTTeam team = teams.get(participant.getTeamId());
            return CommandResult.failure(Component.empty()
                    .append(Component.text("Can't join "))
                    .append(Component.text(gameType.getTitle()))
                    .append(Component.text(" because "))
                    .append(team.getFormattedDisplayName())
                    .append(Component.text(" is in "))
                    .append(Component.text(teamGameType.getTitle())));
        }
        onParticipantJoinGame(gameType, participant);
        activeGame.onTeamJoin(teams.get(participant.getTeamId()));
        activeGame.onParticipantJoin(participant);
        return CommandResult.success(Component.empty()
                .append(Component.text("Joining "))
                .append(Component.text(gameType.getTitle())));
    }
    
    public CommandResult joinAdminToGame(@NotNull GameType gameType, @NotNull Player admin) {
        if (isAdminInGame(admin)) {
            return CommandResult.failure(Component.text("Already in a game"));
        }
        MCTGame activeGame = activeGames.get(gameType);
        if (activeGame == null) {
            return CommandResult.failure(Component.empty()
                    .append(Component.text("No "))
                    .append(Component.text(gameType.getTitle()))
                    .append(Component.text(" game is active right now")));
        }
        onAdminJoinGame(gameType, admin);
        activeGame.onAdminJoin(admin);
        return CommandResult.success(Component.empty()
                .append(Component.text("Joining "))
                .append(Component.text(gameType.getTitle())));
    }
    
    public CommandResult returnParticipantToHub(@NotNull MCTParticipant participant) {
        return returnParticipantToHub(participant, config.getSpawn());
    }
    
    protected CommandResult returnParticipantToHub(@NotNull MCTParticipant participant, @NotNull Location spawn) {
        GameType gameType = participantGames.remove(participant.getUniqueId());
        if (gameType == null) {
            participant.teleport(spawn);
            return CommandResult.success(Component.text("Returning to hub"));
        }
        MCTGame activeGame = activeGames.get(gameType);
        if (activeGame == null) {
            // this should not happen
            participant.teleport(spawn);
            return CommandResult.success(Component.text("Returning to hub"));
        }
        activeGame.onParticipantQuit(participant.getUniqueId());
        onParticipantReturnToHub(participant, spawn);
        activeGame.onTeamQuit(participant.getTeamId());
        return CommandResult.success(Component.text("Quitting current game. Returning to hub."));
    }
    
    public CommandResult returnAdminToHub(@NotNull Player admin) {
        return returnAdminToHub(admin, config.getSpawn());
    }
    
    public CommandResult returnAdminToHub(@NotNull Player admin, @NotNull Location spawn) {
        GameType gameType = adminGames.remove(admin.getUniqueId());
        if (gameType == null) {
            admin.teleport(spawn);
            return CommandResult.success(Component.text("Returning to hub"));
        }
        MCTGame activeGame = activeGames.get(gameType);
        if (activeGame == null) {
            // this should not happen
            admin.teleport(spawn);
            return CommandResult.success(Component.text("Returning to hub"));
        }
        activeGame.onAdminQuit(admin);
        onAdminReturnToHub(admin, spawn);
        return CommandResult.success(Component.text("Quitting current game. Returning to hub."));
    }
    // participant stop
    
    // admin start
    /**
     * Adds the given player as an admin. If the player is already an admin, nothing happens. If the player is a participant, they are removed from their team and added as an admin.
     * @param newAdmin The player to add
     */
    public CommandResult addAdmin(Player newAdmin) {
        UUID uniqueId = newAdmin.getUniqueId();
        if (gameStateStorageUtil.isAdmin(uniqueId)) {
            return CommandResult.failure(Component.empty()
                    .append(Component.text(newAdmin.getName())
                            .decorate(TextDecoration.BOLD))
                    .append(Component.text(" is already an admin. Nothing happened.")));
        }
        List<CommandResult> results = new ArrayList<>();
        OfflineParticipant offlineParticipant = allParticipants.get(uniqueId);
        if (offlineParticipant != null) {
            results.add(leaveParticipant(offlineParticipant));
        }
        try {
            gameStateStorageUtil.addAdmin(uniqueId);
        } catch (ConfigIOException e) {
            context.reportGameStateException("adding new admin", e);
            results.add(CommandResult.failure(Component.text("error occurred adding new admin, see console for details.")));
            return CompositeCommandResult.all(results);
        }
        
        org.bukkit.scoreboard.Team adminTeam = mctScoreboard.getTeam(GameManager.ADMIN_TEAM);
        if (adminTeam != null) {
            adminTeam.addPlayer(newAdmin);
        } else {
            Main.logger().warning(String.format("mctScoreboard could not find team \"%s\" (addAdmin)", GameManager.ADMIN_TEAM));;
        }
        if (newAdmin.isOnline()) {
            newAdmin.sendMessage(Component.text("You were added as an admin"));
            onAdminJoin(newAdmin);
        }
        
        results.add(CommandResult.success(Component.empty()
                .append(Component.text("Added "))
                .append(Component.text(newAdmin.getName())
                        .decorate(TextDecoration.BOLD))
                .append(Component.text(" as an admin"))));
        return CompositeCommandResult.all(results);
    }
    
    /**
     * Removes the given player from the admins
     * @param offlineAdmin The admin to remove
     */
    public CommandResult removeAdmin(@NotNull OfflinePlayer offlineAdmin, String adminName) {
        if (!context.isAdmin(offlineAdmin.getUniqueId())) {
            return CommandResult.failure(Component.empty()
                    .append(Component.text(adminName)
                            .decorate(TextDecoration.BOLD))
                    .append(Component.text(" is not an admin. Nothing happened.")));
        }
        if (offlineAdmin.isOnline()) {
            Player onlineAdmin = offlineAdmin.getPlayer();
            if (onlineAdmin != null) {
                onlineAdmin.sendMessage(Component.text("You were removed as an admin"));
                onAdminQuit(onlineAdmin);
            }
        }
        UUID adminUniqueId = offlineAdmin.getUniqueId();
        try {
            gameStateStorageUtil.removeAdmin(adminUniqueId);
        } catch (ConfigIOException e) {
            context.reportGameStateException("removing admin", e);
            return CommandResult.failure(Component.text("error occurred removing admin, see console for details."));
        }
        
        org.bukkit.scoreboard.Team adminTeam = mctScoreboard.getTeam(GameManager.ADMIN_TEAM);
        if (adminTeam != null) {
            adminTeam.removePlayer(offlineAdmin);
        } else {
            Main.logger().warning(String.format("mctScoreboard could not find team \"%s\" (addAdmin)", GameManager.ADMIN_TEAM));
        }
        
        return CommandResult.success(Component.empty()
                .append(Component.text(adminName)
                        .decorate(TextDecoration.BOLD))
                .append(Component.text(" is no longer an admin")));
    }
    // admin stop
    
    // event handlers start
    public void onParticipantDeath(PlayerDeathEvent event, MCTParticipant participant) {
        GameManagerUtils.replaceWithDisplayName(event, participant);
        if (participant.getKiller() != null) {
            MCTParticipant killer = onlineParticipants.get(participant.getKiller().getUniqueId());
            if (killer != null) {
                GameManagerUtils.replaceWithDisplayName(event, killer);
            }
        }
        GameManagerUtils.deColorLeatherArmor(event.getDrops());
    }
    
    public boolean isParticipantInGame(Participant participant) {
        return isParticipantInGame(participant.getUniqueId());
    }
    
    public boolean isParticipantInGame(UUID uuid) {
        return participantGames.containsKey(uuid);
    }
    
    public boolean isAdminInGame(Player admin) {
        return isAdminInGame(admin.getUniqueId());
    }
    
    public boolean isAdminInGame(UUID uuid) {
        return adminGames.containsKey(uuid);
    }
    
    public void onParticipantRespawn(PlayerRespawnEvent event, MCTParticipant participant) {
        if (isParticipantInGame(participant)) {
            return;
        }
        event.setRespawnLocation(config.getSpawn());
    }
    
    public void onParticipantDamage(@NotNull EntityDamageEvent event, MCTParticipant participant) {
        if (isParticipantInGame(participant)) {
            return;
        }
        Main.debugLog(LogType.CANCEL_ENTITY_DAMAGE_EVENT, "GameManagerState.onParticipantDamage()->participant is in hub cancelled");
        event.setCancelled(true);
    }
    
    public void onParticipantInteract(@NotNull PlayerInteractEvent event, MCTParticipant participant) {
        if (isParticipantInGame(participant)) {
            return;
        }
        Block clickedBlock = event.getClickedBlock();
        if (clickedBlock == null) {
            return;
        }
        Material blockType = clickedBlock.getType();
        if (!config.getPreventInteractions().contains(blockType)) {
            return;
        }
        event.setCancelled(true);
    }
    
    public void onParticipantInventoryClick(@NotNull InventoryClickEvent event, MCTParticipant participant) {
        // do nothing
    }
    
    public void onParticipantDropItem(@NotNull PlayerDropItemEvent event, MCTParticipant participant) {
        // do nothing
    }
    
    public void onParticipantFoodLevelChange(@NotNull FoodLevelChangeEvent event, MCTParticipant participant) {
        if (isParticipantInGame(participant)) {
            return;
        }
        participant.setFoodLevel(20);
        event.setCancelled(true);
    }
    
    public void onParticipantMove(@NotNull PlayerMoveEvent event, MCTParticipant participant) {
        if (isParticipantInGame(participant)) {
            return;
        }
        Location location = participant.getLocation();
        if (location.getY() < config.getYLimit()) {
            participant.teleport(config.getSpawn());
            participant.sendMessage("You fell out of the hub boundary");
        }
    }
    
    public double getMultiplier() {
        return 1.0;
    }
    // event handlers stop
}
