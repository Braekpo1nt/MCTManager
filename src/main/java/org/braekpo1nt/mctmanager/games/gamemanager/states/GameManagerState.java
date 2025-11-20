package org.braekpo1nt.mctmanager.games.gamemanager.states;

import lombok.Setter;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.commands.CommandUtils;
import org.braekpo1nt.mctmanager.commands.manager.commandresult.CommandResult;
import org.braekpo1nt.mctmanager.commands.manager.commandresult.CompositeCommandResult;
import org.braekpo1nt.mctmanager.config.Config;
import org.braekpo1nt.mctmanager.config.exceptions.ConfigIOException;
import org.braekpo1nt.mctmanager.config.exceptions.ConfigInvalidException;
import org.braekpo1nt.mctmanager.database.entities.FinalPersonalScore;
import org.braekpo1nt.mctmanager.database.entities.FinalTeamScore;
import org.braekpo1nt.mctmanager.database.entities.GameSession;
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
import org.braekpo1nt.mctmanager.games.game.footrace.editor.FootRaceEditor;
import org.braekpo1nt.mctmanager.games.game.interfaces.GameEditor;
import org.braekpo1nt.mctmanager.games.game.interfaces.MCTGame;
import org.braekpo1nt.mctmanager.games.game.parkourpathway.ParkourPathwayGame;
import org.braekpo1nt.mctmanager.games.game.parkourpathway.config.ParkourPathwayConfig;
import org.braekpo1nt.mctmanager.games.game.parkourpathway.config.ParkourPathwayConfigController;
import org.braekpo1nt.mctmanager.games.game.parkourpathway.editor.ParkourPathwayEditor;
import org.braekpo1nt.mctmanager.games.game.spleef.SpleefGame;
import org.braekpo1nt.mctmanager.games.game.spleef.config.SpleefConfig;
import org.braekpo1nt.mctmanager.games.game.spleef.config.SpleefConfigController;
import org.braekpo1nt.mctmanager.games.game.survivalgames.SurvivalGamesGame;
import org.braekpo1nt.mctmanager.games.game.survivalgames.config.SurvivalGamesConfig;
import org.braekpo1nt.mctmanager.games.game.survivalgames.config.SurvivalGamesConfigController;
import org.braekpo1nt.mctmanager.games.gamemanager.GameInstanceId;
import org.braekpo1nt.mctmanager.games.gamemanager.GameManager;
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
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.scoreboard.Scoreboard;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.stream.Collectors;

public abstract class GameManagerState {
    
    protected final @NotNull GameManager context;
    protected final @NotNull ContextReference contextReference;
    protected final @NotNull TabList tabList;
    protected final Scoreboard mctScoreboard;
    
    protected final Map<String, MCTTeam> teams;
    protected final Map<UUID, OfflineParticipant> allParticipants;
    protected final Map<UUID, MCTParticipant> onlineParticipants;
    protected final List<Player> onlineAdmins;
    protected final Main plugin;
    protected final GameStateStorageUtil gameStateStorageUtil;
    protected final SidebarFactory sidebarFactory;
    protected final List<LeaderboardManager> leaderboardManagers;
    protected final Sidebar sidebar;
    protected final @NotNull Map<GameInstanceId, MCTGame> activeGames;
    /**
     * A reference to which participant is in which game<br>
     * If a participant's UUID is a key in this map, that participant is
     * in a game.
     */
    protected final Map<UUID, GameInstanceId> participantGames;
    /**
     * A reference to which admin is in which game<br>
     * If an admin's UUID is a key in this map, that admin is in
     * a game.
     */
    protected final Map<UUID, GameInstanceId> adminGames;
    /**
     * A reference to which admin is in which editor<br>
     * If an admin's UUID is a key in this map, that admin is in
     * an editor.
     */
    protected final Map<UUID, GameInstanceId> adminEditors;
    @Setter
    protected @NotNull HubConfig config;
    
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
        this.adminEditors = contextReference.getAdminEditors();
        this.plugin = contextReference.getPlugin();
        this.activeGames = contextReference.getActiveGames();
        this.gameStateStorageUtil = contextReference.getGameStateStorageUtil();
        this.sidebarFactory = contextReference.getSidebarFactory();
        
        this.teams = contextReference.getTeams();
        this.allParticipants = contextReference.getAllParticipants();
        this.onlineParticipants = contextReference.getOnlineParticipants();
        this.onlineAdmins = contextReference.getOnlineAdmins();
        this.config = context.getConfig();
    }
    
    /**
     * Called after this state is assigned to the context
     */
    public abstract void enter();
    
    /**
     * Called before this state is un-assigned from the context
     */
    public abstract void exit();
    
    public abstract CommandResult switchMode(@NotNull String mode);
    
    public abstract @NotNull String getMode();
    
    public void cleanup() {
        this.leaderboardManagers.forEach(LeaderboardManager::tearDown);
        this.tabList.cleanup();
        this.sidebar.deleteAllLines();
        this.onlineAdmins.clear();
        this.onlineParticipants.clear();
        this.teams.clear();
        this.allParticipants.clear();
    }
    
    public void onLoadGameState() {
        // do nothing
    }
    
    // leave/join start
    public final void onAdminJoin(@NotNull PlayerJoinEvent event, @NotNull Player admin) {
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
    
    public final void onParticipantJoin(@NotNull PlayerJoinEvent event, @NotNull MCTParticipant participant) {
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
    
    public final void onAdminQuit(@NotNull PlayerQuitEvent event, @NotNull Player admin) {
        event.quitMessage(GameManagerUtils.replaceWithDisplayName(admin, event.quitMessage()));
        onAdminQuit(admin);
    }
    
    public void onAdminQuit(@NotNull Player admin) {
        GameInstanceId id = adminGames.get(admin.getUniqueId());
        if (id != null) {
            MCTGame activeGame = activeGames.get(id);
            if (activeGame != null) {
                activeGame.onAdminQuit(admin);
            }
            onAdminReturnToHub(admin);
        }
        onlineAdmins.remove(admin);
        GameInstanceId gameInstanceId = adminGames.get(admin.getUniqueId());
        if (gameInstanceId != null) {
            MCTGame game = activeGames.get(gameInstanceId);
            if (game != null) {
                game.onAdminQuit(admin);
            }
        }
        GameInstanceId editorId = adminEditors.get(admin.getUniqueId());
        if (editorId != null) {
            if (context.getActiveEditor() != null) {
                context.getActiveEditor().onAdminQuit(admin.getUniqueId());
            }
        }
        tabList.hidePlayer(admin.getUniqueId());
        sidebar.removePlayer(admin);
        Component displayName = Component.text(admin.getName(), NamedTextColor.WHITE);
        admin.displayName(displayName);
        admin.playerListName(displayName);
    }
    
    public final void onParticipantQuit(@NotNull PlayerQuitEvent event, @NotNull MCTParticipant participant) {
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
        GameInstanceId id = participantGames.get(participant.getUniqueId());
        if (id != null) {
            MCTGame activeGame = activeGames.get(id);
            if (activeGame != null) {
                activeGame.onParticipantQuit(participant.getUniqueId());
                activeGame.onTeamQuit(participant.getTeamId());
            }
            onParticipantReturnToHub(participant);
            participant.teleport(config.getSpawn());
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
    public void updateScoreVisuals(Collection<? extends Team> mctTeams, Collection<? extends Participant> mctParticipants) {
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
            teamLines[i] = new KeyLine("team" + i, Component.empty()
                    .append(team.getFormattedDisplayName())
                    .append(Component.text(": "))
                    .append(Component.text(team.getScore())
                            .color(NamedTextColor.GOLD))
            );
        }
        // fill out any empty lines
        for (int i = numOfTeamLines; i < 10; i++) {
            teamLines[i] = new KeyLine("team" + i, Component.empty());
        }
        sidebar.updateLines(teamLines);
    }
    
    /**
     * Updates the sidebars of the given participants, unless they are in a game
     * @param updateParticipants the participants to update the sidebars of
     */
    public void updateSidebarPersonalScores(Collection<? extends Participant> updateParticipants) {
        for (Participant participant : updateParticipants) {
            if (!isParticipantInGame(participant)) {
                sidebar.updateLine(participant.getUniqueId(), "personalScore",
                        Component.empty()
                                .append(Component.text("Personal: "))
                                .append(Component.text(participant.getScore()))
                                .color(NamedTextColor.GOLD));
            }
        }
    }
    
    protected void displayStats(Map<String, Integer> teamScores, Map<UUID, Integer> participantScores, @NotNull GameInstanceId id) {
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
                    .append(Component.text(i + 1))
                    .append(Component.text(". "))
                    .append(team.getFormattedDisplayName())
                    .append(Component.text(": "))
                    .append(Component.text(teamScores.get(team.getTeamId()))
                            .color(NamedTextColor.GOLD))
                    .append(Component.newline());
        }
        
        // TODO: adjust output to not show scores of 0 instead of specifically farm rush
        if (id.getGameType() == GameType.FARM_RUSH) {
            Audience.audience(
                    Audience.audience(sortedTeams),
                    plugin.getServer().getConsoleSender()
            ).sendMessage(everyone.build());
            return;
        }
        
        everyone.append(Component.text("\nTop 5 Participants:"))
                .append(Component.newline());
        boolean shouldShowMultiplier = getMultiplier() != 1.0;
        for (int i = 0; i < Math.min(sortedParticipants.size(), 5); i++) {
            OfflineParticipant participant = sortedParticipants.get(i);
            everyone
                    .append(Component.text("  "))
                    .append(Component.text(i + 1))
                    .append(Component.text(". "))
                    .append(participant.displayName())
                    .append(Component.text(": "))
                    .append(Component.text(participantScores.get(participant.getUniqueId()))
                            .color(NamedTextColor.GOLD));
            if (shouldShowMultiplier) {
                everyone
                        .append(Component.text(" ("))
                        .append(Component.text((int) (participantScores.get(participant.getUniqueId()) / getMultiplier()))
                                .color(NamedTextColor.GOLD))
                        .append(Component.text(" x "))
                        .append(Component.text(getMultiplier()))
                        .append(Component.text(")"));
            }
            everyone
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
                                    .color(NamedTextColor.GOLD));
                    if (shouldShowMultiplier) {
                        message
                                .append(Component.text(" ("))
                                .append(Component.text((int) (participantScores.get(participant.getUniqueId()) / getMultiplier()))
                                        .color(NamedTextColor.GOLD))
                                .append(Component.text(" x "))
                                .append(Component.text(getMultiplier()))
                                .append(Component.text(")"));
                    }
                    message
                            .append(Component.newline());
                    i++;
                }
            }
            team.sendMessage(message.build());
        }
        
        for (OfflineParticipant offlineParticipant : sortedParticipants) {
            Participant participant = onlineParticipants.get(offlineParticipant.getUniqueId());
            if (participant != null) {
                TextComponent.Builder message = Component.text();
                message
                        .append(Component.text("Personal")
                                .color(NamedTextColor.GOLD))
                        .append(Component.text(": "))
                        .append(Component.text(participantScores.get(offlineParticipant.getUniqueId()))
                                .color(NamedTextColor.GOLD));
                if (shouldShowMultiplier) {
                    message
                            .append(Component.text(" ("))
                            .append(Component.text((int) (participantScores.get(offlineParticipant.getUniqueId()) / getMultiplier()))
                                    .color(NamedTextColor.GOLD))
                            .append(Component.text(" x "))
                            .append(Component.text(getMultiplier()))
                            .append(Component.text(")"));
                }
                participant.sendMessage(message);
            }
        }
    }
    // ui end
    
    // game start
    public CommandResult startGame(@NotNull Set<String> teamIds, @NotNull List<Player> gameAdmins, @NotNull GameType gameType, @NotNull String configFile) {
        GameInstanceId gameInstanceId = new GameInstanceId(gameType, configFile);
        if (teamIds.isEmpty()) {
            return CommandResult.failure("Can't start a game with no teams.");
        }
        
        if (activeGames.containsKey(gameInstanceId)) {
            return CommandResult.failure(Component.text("There is already a ")
                    .append(Component.text(gameInstanceId.getTitle()))
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
        
        instantiateGame(
                gameInstanceId,
                new HashSet<>(gameTeams),
                new HashSet<>(gameParticipants),
                gameAdmins);
        return CommandResult.success();
    }
    
    public CommandResult startEditor(@NotNull GameType gameType, @NotNull String configFile) {
        GameInstanceId gameInstanceId = new GameInstanceId(gameType, configFile);
        if (!activeGames.isEmpty()) {
            return CommandResult.failure(Component.text("Can't start an editor while any games are active"));
        }
        
        if (onlineAdmins.isEmpty()) {
            return CommandResult.failure(Component.text("There are no online admins. You can add admins using:\n")
                    .append(Component.text("/mct admin add <member>")
                            .decorate(TextDecoration.BOLD)
                            .clickEvent(ClickEvent.suggestCommand("/mct admin add "))));
        }
        
        if (editorIsRunning()) {
            return CommandResult.failure(Component.text("An editor is already running. You must stop it before you can start another one."));
        }
        
        if (!editorExists(gameType)) {
            return CommandResult.failure(Component.text("Can't find editor for game type " + gameType));
        }
        
        for (Player admin : onlineAdmins) {
            onAdminJoinEditor(gameInstanceId, admin);
        }
        
        try {
            context.setActiveEditor(instantiateEditor(
                    gameType,
                    configFile,
                    new ArrayList<>(onlineAdmins)
            ));
        } catch (Exception e) {
            for (Player admin : onlineAdmins) {
                onAdminReturnToHub(admin);
            }
            Main.logger().log(Level.SEVERE, String.format("Error starting editor %s", gameType), e);
            return CommandResult.failure(Component.text("Can't start ")
                    .append(Component.empty()
                            .append(Component.text(gameType.name()))
                            .decorate(TextDecoration.BOLD))
                    .append(Component.text("'s editor. Error starting editor. See console for details:\n"))
                    .append(Component.text(e.getMessage()))
                    .color(NamedTextColor.RED));
        }
        return CommandResult.success();
    }
    
    protected void onParticipantJoinGame(@NotNull GameInstanceId id, Participant participant) {
        participantGames.put(participant.getUniqueId(), id);
        tabList.hidePlayer(participant);
        sidebar.removePlayer(participant);
    }
    
    protected void onAdminJoinGame(@NotNull GameInstanceId id, Player admin) {
        adminGames.put(admin.getUniqueId(), id);
        tabList.hidePlayer(admin);
        sidebar.removePlayer(admin);
    }
    
    protected void onAdminJoinEditor(@NotNull GameInstanceId id, Player admin) {
        adminEditors.put(admin.getUniqueId(), id);
        tabList.hidePlayer(admin);
        sidebar.removePlayer(admin);
    }
    
    protected void onParticipantReturnToHub(@NotNull Participant participant) {
        participantGames.remove(participant.getUniqueId());
        participant.setGameMode(GameMode.ADVENTURE);
        ParticipantInitializer.clearInventory(participant);
        ParticipantInitializer.resetHealthAndHunger(participant);
        ParticipantInitializer.clearStatusEffects(participant);
        tabList.showPlayer(participant);
        sidebar.addPlayer(participant);
        MCTTeam team = teams.get(participant.getTeamId());
        updateScoreVisuals(Collections.singletonList(team), Collections.singletonList(participant));
    }
    
    protected void onAdminReturnToHub(@NotNull Player admin) {
        adminGames.remove(admin.getUniqueId());
        adminEditors.remove(admin.getUniqueId());
        admin.setGameMode(GameMode.SPECTATOR);
        tabList.showPlayer(admin);
        sidebar.addPlayer(admin);
        updateSidebarTeamScores();
    }
    
    public CommandResult stopGame(@NotNull GameType gameType, @Nullable String configFile) {
        GameInstanceId id;
        if (configFile == null) {
            List<GameInstanceId> activeIds = new ArrayList<>();
            for (GameInstanceId gameInstanceId : activeGames.keySet()) {
                if (gameInstanceId.getGameType().equals(gameType)) {
                    activeIds.add(gameInstanceId);
                }
            }
            if (activeIds.size() == 1) {
                id = activeIds.getFirst();
            } else if (activeIds.isEmpty()) {
                return CommandResult.failure(Component.empty()
                        .append(Component.text("No "))
                        .append(Component.text(gameType.getTitle()))
                        .append(Component.text(" game is active right now")));
            } else {
                return CommandResult.failure(Component.empty()
                        .append(Component.text("More than one instance of "))
                        .append(Component.text(gameType.getTitle()))
                        .append(Component.text(" is active. Please specify a config.")));
            }
        } else {
            id = new GameInstanceId(gameType, configFile);
        }
        MCTGame game = activeGames.get(id);
        if (game == null) {
            return CommandResult.failure(Component.empty()
                    .append(Component.text("No instances of game "))
                    .append(Component.text(id.getTitle())
                            .decorate(TextDecoration.BOLD))
                    .append(Component.text(" are active")));
        }
        game.stop();
        return CommandResult.success(Component.empty()
                .append(Component.text("Stopping "))
                .append(Component.text(id.getTitle()))
                .append(Component.text(" ("))
                .append(Component.text(id.getConfigFile()))
                .append(Component.text(")")));
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
     * @param gameSessionId the id of the {@link GameSession} entity associated with the finished game
     * @param id the instance id of the finished game
     * @param teamScores the team scores
     * @param participantScores the participant scores
     * @param gameParticipants the UUIDs of the participants which are online and were in the finished
     * game. Must be UUIDs which are keys in {@link #onlineParticipants}.
     * @param gameAdmins the admins who were in the game
     */
    public void gameIsOver(int gameSessionId, @NotNull GameInstanceId id, Map<String, Integer> teamScores, Map<UUID, Integer> participantScores, @NotNull Collection<UUID> gameParticipants, @NotNull List<Player> gameAdmins) {
        MCTGame game = activeGames.remove(id);
        if (game == null) {
            return;
        }
        for (UUID uuid : gameParticipants) {
            MCTParticipant participant = onlineParticipants.get(uuid);
            onParticipantReturnToHub(participant);
            participant.teleport(config.getSpawn());
            participant.sendMessage(Component.text("Returning to hub"));
        }
        for (Player admin : gameAdmins) {
            onAdminReturnToHub(admin);
            admin.teleport(config.getSpawn());
            admin.sendMessage(Component.text("Returning to hub"));
        }
        Date endDate = new Date();
        double multiplier = getMultiplier();
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                context.getScoreService().setGameSessionEndDate(gameSessionId, endDate); // TODO: move this to persistScores?
                persistDatabaseScores(
                        teamScores,
                        participantScores,
                        gameSessionId,
                        id,
                        endDate,
                        multiplier
                );
            } catch (SQLException e) {
                Main.logger().log(Level.SEVERE, "An error occurred saving end-game data to the database", e);
                context.messageAdmins(Component.empty()
                        .append(Component.text("An error occurred saving end-game data to te database. See console for details.")));
            }
        });
        addScores(teamScores, participantScores, gameSessionId, id, endDate);
    }
    
    /**
     * Add the given scores to the given teams and participants, save the game state, update
     * the UI, etc. <br>
     * If any invalid teamIds or UUIDs are used, there will be errors
     * @param newTeamScores map of teamId to score to add. Must be teamIds of real teams
     * @param newParticipantScores map of UUID to score to add. Must be UUIDs of real participants
     * @param gameSessionId the id of the {@link GameSession} associated with this game
     * @param id the {@link GameInstanceId}
     * @param endDate the date the game ended
     */
    protected void addScores(
            Map<String, Integer> newTeamScores,
            Map<UUID, Integer> newParticipantScores,
            int gameSessionId,
            @NotNull GameInstanceId id,
            @NotNull Date endDate) {
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
        gameStateStorageUtil.updateScores(teams.values(), allParticipants.values());
        if (plugin.isEnabled()) {
            plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
                try {
                    gameStateStorageUtil.saveGameState();
                } catch (ConfigIOException e) {
                    context.reportGameStateException("updating scores", e);
                }
            });
        } else {
            try {
                gameStateStorageUtil.saveGameState();
            } catch (ConfigIOException e) {
                context.reportGameStateException("updating scores", e);
            }
        }
        updateScoreVisuals(teams.values(), onlineParticipants.values());
        displayStats(teamScores, participantScores, id);
    }
    
    /**
     * Persist the scores to the GameState and the database
     * @param newTeamScores the team scores to persist
     * @param newParticipantScores the participant scores to persist
     * @param gameSessionId the id of the {@link GameSession}
     * @param id the {@link GameInstanceId}
     * @param endDate the time the game ended
     * @param multiplier the multiplier the game used
     * @throws ConfigIOException if there's an issue persisting scores to the GameState
     * @throws SQLException if there's an issue persisting scores to the database
     */
    protected void persistDatabaseScores(
            Map<String, Integer> newTeamScores,
            Map<UUID, Integer> newParticipantScores,
            int gameSessionId,
            GameInstanceId id,
            Date endDate,
            double multiplier
    ) throws SQLException {
        List<FinalPersonalScore> finalPersonalScores = newParticipantScores.entrySet().stream()
                .map(entry -> {
                    OfflineParticipant participant = allParticipants.get(entry.getKey());
                    return FinalPersonalScore.builder()
                            .uuid(entry.getKey().toString())
                            .ign(participant.getName())
                            .teamId(participant.getTeamId())
                            .gameSessionId(gameSessionId)
                            .gameType(id.getGameType())
                            .configFile(id.getConfigFile())
                            .date(endDate)
                            .mode(getMode())
                            .multiplier(multiplier)
                            .points(entry.getValue())
                            .build();
                })
                .toList();
        List<FinalTeamScore> finalTeamScores = newTeamScores.entrySet().stream()
                .map(entry -> FinalTeamScore.builder()
                        .teamId(entry.getKey())
                        .gameSessionId(gameSessionId)
                        .gameType(id.getGameType())
                        .configFile(id.getConfigFile())
                        .date(endDate)
                        .mode(getMode())
                        .multiplier(multiplier)
                        .points((int) (entry.getValue() / multiplier))
                        .build())
                .toList();
        context.getScoreService().logFinalPersonalScores(finalPersonalScores);
        context.getScoreService().logFinalTeamScores(finalTeamScores);
        Main.logger().info("Logged final scores to the database");
    }
    
    /**
     * @param gameInstanceId the {@link GameInstanceId} with the {@link GameType} to instantiate the {@link MCTGame} for
     * and the config file to use
     * @param newTeams the teams to send to the game
     * @param newParticipants the participants to send to the game
     * @param newAdmins the admins to send to the game
     */
    protected void instantiateGame(
            @NotNull GameInstanceId gameInstanceId,
            Collection<Team> newTeams,
            Collection<Participant> newParticipants,
            List<Player> newAdmins) throws ConfigIOException, ConfigInvalidException {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                asyncInstantiateGame(gameInstanceId, newTeams, newParticipants, newAdmins);
            } catch (Exception e) {
                plugin.getServer().getScheduler().runTask(plugin, () -> {
                    onGameInstantiationFailure(gameInstanceId, newParticipants, newAdmins, e);
                });
            }
        });
    }
    
    /**
     * Called if there is an exception thrown while trying to instantiate a game.
     * This is used because games are started asynchronously (so that reading config files
     * and communicating with the database does not lag the server) and thus an error may occur
     * asynchronously, and certain states need to react to this failure in different ways.
     * @param gameInstanceId the {@link GameInstanceId} of the game which failed
     * @param newParticipants the participants who were supposed to be sent to this game
     * @param newAdmins the admins who were supposed to be sent to this game
     * @param e the exception which occurred to cause the failure
     */
    protected void onGameInstantiationFailure(@NotNull GameInstanceId gameInstanceId, Collection<Participant> newParticipants, List<Player> newAdmins, Exception e) {
        Main.logger().log(Level.SEVERE, String.format("Error starting game %s", gameInstanceId), e);
        Audience.audience(
                Audience.audience(newParticipants),
                Audience.audience(context.getOnlineAdmins())
        ).sendMessage(Component.text("Can't start ")
                .append(Component.text(gameInstanceId.getGameType().name())
                        .decorate(TextDecoration.BOLD))
                .append(Component.text(" with config file "))
                .append(Component.text(gameInstanceId.getConfigFile())
                        .decorate(TextDecoration.BOLD))
                .append(Component.text(". Error starting game. See console for details:\n"))
                .append(Component.text(e.getMessage()))
                .color(NamedTextColor.RED));
        Audience.audience(newParticipants).sendMessage(Component.empty()
                .append(Component.text("Contact the admins for support."))
                .color(NamedTextColor.RED));
    }
    
    /**
     * Meant to be called from an asynchronous thread<br>
     * Loads the config file. If that succeeds, creates an entry for the game in the database.
     * If that succeeds, start the game on the main thread.
     * @param gameInstanceId the gameInstanceId
     * @param newTeams the teams to send to the game
     * @param newParticipants the participants to send to the game
     * @param newAdmins the admins to send to the game
     * @throws SQLException if there is an error persisting the {@link GameSession}
     */
    private void asyncInstantiateGame(
            @NotNull GameInstanceId gameInstanceId,
            Collection<Team> newTeams,
            Collection<Participant> newParticipants,
            List<Player> newAdmins) throws SQLException {
        GameType gameType = gameInstanceId.getGameType();
        String configFile = gameInstanceId.getConfigFile();
        Component title = createNewTitle(gameType.getTitle());
        Config config;
        switch (gameType) {
            case SPLEEF -> {
                config = new SpleefConfigController(plugin.getDataFolder(), gameType.getId()).getConfig(configFile);
            }
            case CLOCKWORK -> {
                config = new ClockworkConfigController(plugin.getDataFolder(), gameType.getId()).getConfig(configFile);
            }
            case SURVIVAL_GAMES -> {
                config = new SurvivalGamesConfigController(plugin.getDataFolder(), gameType.getId()).getConfig(configFile);
            }
            case FARM_RUSH -> {
                config = new FarmRushConfigController(plugin.getDataFolder(), gameType.getId()).getConfig(configFile);
            }
            case FOOT_RACE -> {
                config = new FootRaceConfigController(plugin.getDataFolder(), gameType.getId()).getConfig(configFile);
            }
            case PARKOUR_PATHWAY -> {
                config = new ParkourPathwayConfigController(plugin.getDataFolder(), gameType.getId()).getConfig(configFile);
            }
            case CAPTURE_THE_FLAG -> {
                config = new CaptureTheFlagConfigController(plugin.getDataFolder(), gameType.getId()).getConfig(configFile);
            }
            case EXAMPLE -> {
                config = new ExampleConfigController(plugin.getDataFolder(), gameType.getId()).getConfig(configFile);
            }
            case FINAL -> {
                config = new ColossalCombatConfigController(plugin.getDataFolder(), gameType.getId()).getConfig(configFile);
            }
            default -> {
                throw new IllegalArgumentException(String.format("Unsupported GameType %s", gameType));
            }
        }
        
        GameSession gameSession = context.getScoreService().createGameSession(GameSession.builder()
                .gameType(gameType)
                .configFile(configFile)
                .startTime(new Date())
                .mode(getMode())
                .build());
        if (gameSession == null) {
            throw new SQLException("An error occurred creating a GameSession object in the database");
        }
        
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            for (Participant participant : newParticipants) {
                onParticipantJoinGame(gameInstanceId, participant);
            }
            for (Player admin : newAdmins) {
                onAdminJoinGame(gameInstanceId, admin);
            }
            try {
                MCTGame game = switch (gameType) {
                    case SPLEEF -> {
                        yield new SpleefGame(plugin, context, title, gameSession.getId(), (SpleefConfig) config, configFile, newTeams, newParticipants, newAdmins);
                    }
                    case CLOCKWORK -> {
                        yield new ClockworkGame(plugin, context, title, gameSession.getId(), (ClockworkConfig) config, configFile, newTeams, newParticipants, newAdmins);
                    }
                    case SURVIVAL_GAMES -> {
                        yield new SurvivalGamesGame(plugin, context, title, gameSession.getId(), (SurvivalGamesConfig) config, configFile, newTeams, newParticipants, newAdmins);
                    }
                    case FARM_RUSH -> {
                        yield new FarmRushGame(plugin, context, title, gameSession.getId(), (FarmRushConfig) config, configFile, newTeams, newParticipants, newAdmins);
                    }
                    case FOOT_RACE -> {
                        yield new FootRaceGame(plugin, context, title, gameSession.getId(), (FootRaceConfig) config, configFile, newTeams, newParticipants, newAdmins);
                    }
                    case PARKOUR_PATHWAY -> {
                        yield new ParkourPathwayGame(plugin, context, title, gameSession.getId(), (ParkourPathwayConfig) config, configFile, newTeams, newParticipants, newAdmins);
                    }
                    case CAPTURE_THE_FLAG -> {
                        yield new CaptureTheFlagGame(plugin, context, title, gameSession.getId(), (CaptureTheFlagConfig) config, configFile, newTeams, newParticipants, newAdmins);
                    }
                    case EXAMPLE -> {
                        yield new ExampleGame(plugin, context, title, gameSession.getId(), (ExampleConfig) config, configFile, newTeams, newParticipants, newAdmins);
                    }
                    case FINAL -> {
                        List<Team> sortedTeams = newTeams.stream()
                                .sorted((t1, t2) -> {
                                    int scoreComparison = t2.getScore() - t1.getScore();
                                    if (scoreComparison != 0) {
                                        return scoreComparison;
                                    }
                                    return t1.getDisplayName().compareToIgnoreCase(t2.getDisplayName());
                                }).toList();
                        yield new ColossalCombatGame(plugin, context, title, gameSession.getId(), (ColossalCombatConfig) config, configFile, sortedTeams.getFirst(), sortedTeams.get(1), sortedTeams, newParticipants, newAdmins);
                    }
                };
                activeGames.put(gameInstanceId, game);
            } catch (Exception e) {
                for (Participant participant : newParticipants) {
                    onParticipantReturnToHub(participant);
                }
                for (Player admin : newAdmins) {
                    onAdminReturnToHub(admin);
                }
                onGameInstantiationFailure(gameInstanceId, newParticipants, newAdmins, e);
            }
        });
    }
    
    /**
     * @param gameType the game type to check for an editor of
     * @return true if an editor for the given game type exists, false otherwise
     */
    protected boolean editorExists(@NotNull GameType gameType) {
        return switch (gameType) {
            case PARKOUR_PATHWAY,
                 FOOT_RACE -> true;
            default -> false;
        };
    }
    
    public boolean editorIsRunning() {
        return context.getActiveEditor() != null;
    }
    
    /**
     * @param gameType the game type to get the {@link GameEditor} for
     * @return the {@link GameEditor} associated with the given type, or null if there is no editor for the
     * given type (or if the type is null).
     */
    protected @Nullable GameEditor instantiateEditor(
            @NotNull GameType gameType,
            String configFile,
            Collection<Player> newAdmins
    ) {
        return switch (gameType) {
            case PARKOUR_PATHWAY -> {
                ParkourPathwayConfig config = new ParkourPathwayConfigController(plugin.getDataFolder(), gameType.getId()).getConfig(configFile);
                yield new ParkourPathwayEditor(
                        plugin,
                        context,
                        config,
                        configFile,
                        newAdmins
                );
            }
            case FOOT_RACE -> {
                FootRaceConfig config = new FootRaceConfigController(plugin.getDataFolder(), gameType.getId()).getConfig(configFile);
                yield new FootRaceEditor(
                        plugin,
                        context,
                        config,
                        configFile,
                        newAdmins
                );
            }
            default -> null;
        };
    }
    
    protected @NotNull Component createNewTitle(String baseTitle) {
        return Component.empty()
                .append(Component.text(baseTitle))
                .color(NamedTextColor.BLUE);
    }
    // game end
    
    // editor start
    public CommandResult stopEditor() {
        if (!editorIsRunning()) {
            return CommandResult.failure(Component.text("No editor is running"));
        }
        context.getActiveEditor().stop();
        context.setActiveEditor(null);
        return CommandResult.success();
    }
    
    /**
     * Called by an active editor when the editor is ended
     * @param editorAdmins the admins who were in the editor when it ended
     */
    public void editorIsOver(@NotNull Collection<Player> editorAdmins) {
        for (Player admin : editorAdmins) {
            onAdminReturnToHub(admin);
        }
    }
    
    public CommandResult validateEditor(@NotNull String configFile) {
        if (!editorIsRunning()) {
            return CommandResult.failure(Component.text("No editor is running."));
        }
        return context.getActiveEditor().configIsValid(configFile);
    }
    
    public CommandResult saveEditor(@NotNull String configFile, boolean skipValidation) {
        if (!editorIsRunning()) {
            return CommandResult.failure(Component.text("No editor is running."));
        }
        return context.getActiveEditor().saveConfig(configFile, skipValidation);
    }
    
    public CommandResult loadEditor(@NotNull String configFile) {
        if (!editorIsRunning()) {
            return CommandResult.failure(Component.text("No editor is running"));
        }
        return context.getActiveEditor().loadConfig(configFile);
    }
    // editor end
    
    // commands start
    public CommandResult top(@NotNull MCTParticipant participant) {
        GameInstanceId id = participantGames.get(participant.getUniqueId());
        if (id == null) {
            return CommandResult.failure("Can't use /top at this time");
        }
        MCTGame game = activeGames.get(id);
        return game.top(participant.getUniqueId());
    }
    // commands end
    
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
    
    public int getGameIterations(@NotNull GameInstanceId id) {
        return -1;
    }
    
    public CommandResult undoGame(@NotNull GameInstanceId id, int iterationIndex) {
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
     * Joins the given player to the team with the given teamId. If the player was on a team already (not teamId) they
     * will be removed from that team and added to the other team.
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
    
    public CommandResult joinParticipantToGame(@NotNull GameType gameType, @Nullable String configFile, @NotNull MCTParticipant participant) {
        if (isParticipantInGame(participant)) {
            return CommandResult.failure(Component.text("Already in a game"));
        }
        GameInstanceId id;
        if (configFile == null) {
            List<GameInstanceId> activeIds = new ArrayList<>();
            for (GameInstanceId gameInstanceId : activeGames.keySet()) {
                if (gameInstanceId.getGameType().equals(gameType)) {
                    activeIds.add(gameInstanceId);
                }
            }
            if (activeIds.size() == 1) {
                id = activeIds.getFirst();
            } else if (activeIds.isEmpty()) {
                return CommandResult.failure(Component.empty()
                        .append(Component.text("No "))
                        .append(Component.text(gameType.getTitle()))
                        .append(Component.text(" game is active right now")));
            } else {
                return CommandResult.failure(Component.empty()
                        .append(Component.text("More than one instance of "))
                        .append(Component.text(gameType.getTitle()))
                        .append(Component.text(" is active. Please specify a config.")));
            }
        } else {
            id = new GameInstanceId(gameType, configFile);
        }
        MCTGame activeGame = activeGames.get(id);
        if (activeGame == null) {
            return CommandResult.failure(Component.empty()
                    .append(Component.text("No "))
                    .append(Component.text(id.getTitle()))
                    .append(Component.text(" game is active right now")));
        }
        @Nullable GameInstanceId teamGameId = context.getTeamActiveGame(participant.getTeamId());
        if (teamGameId != null && !teamGameId.equals(id)) {
            MCTTeam team = teams.get(participant.getTeamId());
            return CommandResult.failure(Component.empty()
                    .append(Component.text("Can't join "))
                    .append(Component.text(id.getTitle()))
                    .append(Component.text(" because "))
                    .append(team.getFormattedDisplayName())
                    .append(Component.text(" is in "))
                    .append(Component.text(teamGameId.getTitle())));
        }
        onParticipantJoinGame(id, participant);
        activeGame.onTeamJoin(teams.get(participant.getTeamId()));
        activeGame.onParticipantJoin(participant);
        return CommandResult.success(Component.empty()
                .append(Component.text("Joining "))
                .append(Component.text(id.getTitle())));
    }
    
    public @Nullable List<String> tabCompleteActiveGames(@NotNull String[] args) {
        if (args.length == 1) {
            return CommandUtils.partialMatchTabList(
                    activeGames.keySet().stream()
                            .map(id -> id.getGameType().getId())
                            .toList(),
                    args[0]);
        }
        if (args.length == 2) {
            return CommandUtils.partialMatchTabList(
                    activeGames.keySet().stream()
                            .map(GameInstanceId::getConfigFile)
                            .toList(),
                    args[1]);
        }
        
        return Collections.emptyList();
    }
    
    public CommandResult joinAdminToGame(@NotNull GameType gameType, @Nullable String configFile, @NotNull Player admin) {
        if (isAdminInGame(admin)) {
            return CommandResult.failure(Component.text("Already in a game"));
        }
        GameInstanceId id;
        if (configFile == null) {
            List<GameInstanceId> activeIds = new ArrayList<>();
            for (GameInstanceId gameInstanceId : activeGames.keySet()) {
                if (gameInstanceId.getGameType().equals(gameType)) {
                    activeIds.add(gameInstanceId);
                }
            }
            if (activeIds.size() == 1) {
                id = activeIds.getFirst();
            } else if (activeIds.isEmpty()) {
                return CommandResult.failure(Component.empty()
                        .append(Component.text("No "))
                        .append(Component.text(gameType.getTitle()))
                        .append(Component.text(" game is active right now")));
            } else {
                return CommandResult.failure(Component.empty()
                        .append(Component.text("More than one instance of "))
                        .append(Component.text(gameType.getTitle()))
                        .append(Component.text(" is active. Please specify a config.")));
            }
        } else {
            id = new GameInstanceId(gameType, configFile);
        }
        MCTGame activeGame = activeGames.get(id);
        if (activeGame == null) {
            return CommandResult.failure(Component.empty()
                    .append(Component.text("No "))
                    .append(Component.text(id.getTitle()))
                    .append(Component.text(" game is active right now")));
        }
        onAdminJoinGame(id, admin);
        activeGame.onAdminJoin(admin);
        return CommandResult.success(Component.empty()
                .append(Component.text("Joining "))
                .append(Component.text(id.getTitle())));
    }
    
    public CommandResult returnParticipantToHub(@NotNull MCTParticipant participant) {
        return returnParticipantToHub(participant, config.getSpawn());
    }
    
    protected CommandResult returnParticipantToHub(@NotNull MCTParticipant participant, @NotNull Location spawn) {
        GameInstanceId id = participantGames.remove(participant.getUniqueId());
        if (id == null) {
            participant.teleport(spawn);
            return CommandResult.success(Component.text("Returning to hub"));
        }
        MCTGame activeGame = activeGames.get(id);
        if (activeGame == null) {
            // this should not happen
            participant.teleport(spawn);
            return CommandResult.success(Component.text("Returning to hub"));
        }
        activeGame.onParticipantQuit(participant.getUniqueId());
        onParticipantReturnToHub(participant);
        participant.teleport(spawn);
        activeGame.onTeamQuit(participant.getTeamId());
        return CommandResult.success(Component.text("Quitting current game. Returning to hub."));
    }
    
    public CommandResult returnAdminToHub(@NotNull Player admin) {
        return returnAdminToHub(admin, config.getSpawn());
    }
    
    public CommandResult returnAdminToHub(@NotNull Player admin, @NotNull Location spawn) {
        GameInstanceId id = adminGames.remove(admin.getUniqueId());
        if (id == null) {
            admin.teleport(spawn);
            return CommandResult.success(Component.text("Returning to hub"));
        }
        MCTGame activeGame = activeGames.get(id);
        if (activeGame == null) {
            // this should not happen
            admin.teleport(spawn);
            return CommandResult.success(Component.text("Returning to hub"));
        }
        activeGame.onAdminQuit(admin);
        onAdminReturnToHub(admin);
        admin.teleport(spawn);
        return CommandResult.success(Component.text("Quitting current game. Returning to hub."));
    }
    // participant stop
    
    // admin start
    
    /**
     * Adds the given player as an admin. If the player is already an admin, nothing happens. If the player is a
     * participant, they are removed from their team and added as an admin.
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
            Main.logger().warning(String.format("mctScoreboard could not find team \"%s\" (addAdmin)", GameManager.ADMIN_TEAM));
            ;
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
    
    public void onAdminDamage(@NotNull EntityDamageEvent event, @NotNull Player admin) {
        Main.debugLog(LogType.CANCEL_ENTITY_DAMAGE_EVENT, "GameManagerState.onAdminDamage()->admin");
        event.setCancelled(true);
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
        if (clickedBlock != null) {
            Material blockType = clickedBlock.getType();
            if (config.getPreventInteractions().contains(blockType)) {
                event.setUseInteractedBlock(Event.Result.DENY);
            }
        }
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
    
    public List<GameType> getVotingPool() {
        return Collections.emptyList();
    }
    // event handlers stop
}
