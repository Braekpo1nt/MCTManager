package org.braekpo1nt.mctmanager.games.base;

import com.destroystokyo.paper.event.player.PlayerPostRespawnEvent;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.commands.manager.commandresult.CommandResult;
import org.braekpo1nt.mctmanager.config.SpectatorBoundary;
import org.braekpo1nt.mctmanager.games.gamemanager.GameInstanceId;
import org.braekpo1nt.mctmanager.games.gamemanager.GameManager;
import org.braekpo1nt.mctmanager.games.base.listeners.GameListener;
import org.braekpo1nt.mctmanager.games.base.states.GameStateBase;
import org.braekpo1nt.mctmanager.games.game.enums.GameType;
import org.braekpo1nt.mctmanager.games.game.interfaces.MCTGame;
import org.braekpo1nt.mctmanager.games.utils.GameManagerUtils;
import org.braekpo1nt.mctmanager.games.utils.ParticipantInitializer;
import org.braekpo1nt.mctmanager.participant.*;
import org.braekpo1nt.mctmanager.ui.UIManager;
import org.braekpo1nt.mctmanager.ui.sidebar.KeyLine;
import org.braekpo1nt.mctmanager.ui.sidebar.Sidebar;
import org.braekpo1nt.mctmanager.ui.tablist.TabList;
import org.braekpo1nt.mctmanager.ui.timer.TimerManager;
import org.bukkit.GameMode;
import org.bukkit.GameRule;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.logging.Level;

/**
 * @param <P> the ParticipantData implementation used by this game
 * @param <T> the ScoredTeamData implementation used by this game
 */
@Getter
@Setter
public abstract class GameBase<P extends ParticipantData, T extends ScoredTeamData<P>, QP extends QuitDataBase, QT extends QuitDataBase, S extends GameStateBase<P, T>>  implements MCTGame, Listener {
    protected final @NotNull GameType type;
    protected final @NotNull GameInstanceId gameInstanceId;
    protected final @NotNull Main plugin;
    protected final @NotNull GameManager gameManager;
    protected final @NotNull TimerManager timerManager;
    protected final @NotNull Sidebar sidebar;
    protected final @NotNull Sidebar adminSidebar;
    protected final @NotNull TabList tabList;
    protected final @NotNull Map<UUID, P> participants;
    protected final @NotNull Map<ParticipantID, QP> quitDatas;
    protected final @NotNull Map<String, T> teams;
    protected final @NotNull Map<String, QT> teamQuitDatas;
    protected final @NotNull List<Player> admins;
    protected final @NotNull List<UIManager> uiManagers;
    protected final @NotNull List<GameListener<P>> listeners;
    /**
     * <p>The game rules that were changed by {@link #setGameRule(GameRule, Object)},
     * and so must be restored to their state before the game started.
     * The resetting is done in {@link #stop()}.</p>
     */
    protected final @NotNull List<StoredGameRule<?>> storedGameRules;
    
    /**
     * The current state of this game
     */
    protected @NotNull S state;
    protected @NotNull Component title;
    
    /**
     * Initialize data and start the game
     * @param gameInstanceId the {@link GameInstanceId} associated with this game
     * @param plugin the plugin
     * @param gameManager the GameManager
     * @param title the game's initial title, displayed in the sidebar
     * @param initialState the initialization state, should not contain any game functionality.
     *                     The state must never be null, so this is what the state should be
     *                     as the game is being initialized to prevent null-pointer
     *                     exceptions. 
     */
    public GameBase(
            @NotNull GameInstanceId gameInstanceId,
            @NotNull Main plugin,
            @NotNull GameManager gameManager,
            @NotNull Component title,
            @NotNull S initialState) {
        this.type = gameInstanceId.getGameType();
        this.gameInstanceId = gameInstanceId;
        this.plugin = plugin;
        this.gameManager = gameManager;
        this.sidebar = gameManager.createSidebar();
        this.adminSidebar = gameManager.createSidebar();
        this.tabList = new TabList(plugin);
        this.participants = new HashMap<>();
        this.quitDatas = new HashMap<>();
        this.teams = new HashMap<>();
        this.teamQuitDatas = new HashMap<>();
        this.title = title;
        this.uiManagers = new ArrayList<>();
        this.timerManager = gameManager.getTimerManager().register(new TimerManager(plugin));
        this.admins = new ArrayList<>();
        this.listeners = new ArrayList<>();
        this.storedGameRules = new ArrayList<>();
        this.state = initialState;
    }
    
    @Override
    public boolean containsTeam(@NotNull String teamId) {
        return teams.containsKey(teamId);
    }
    
    /**
     * <p>Add a new {@link GameListener} component to this game.
     * This will be registered on {@link #start(Collection, Collection, List)},
     * and will be unregistered on {@link #stop()}.</p>
     * <p>Note: Calling after {@link #start(Collection, Collection, List)} 
     * won't register the listener.</p>
     * @param listener the listener to register
     */
    public void addListener(GameListener<P> listener) {
        this.listeners.add(listener);
    }
    
    /**
     * <p>Set the game rule of {@link #getWorld()} using the given rule and value.
     * Each rule set in this way will be reset to the value it was before
     * this game started (see {@link #stop()}).</p>
     * @param rule the game rule
     * @param value the value
     * @param <G> the type that the game rule's value should be
     */
    protected <G> void setGameRule(@NotNull GameRule<G> rule, @NotNull G value) {
        G oldValue = getWorld().getGameRuleValue(rule);
        storedGameRules.add(new StoredGameRule<>(rule, oldValue));
        getWorld().setGameRule(rule, value);
    }
    
    protected abstract @NotNull World getWorld();
    
    /**
     * <p>Call this after all fields have been initialized. 
     * This initializes all the participants and teams, 
     * and finally assigns {@link #getStartState()} to {@link #state}.</p>
     * @param newTeams the teams going into the game
     * @param newParticipants the participants going into the game
     * @param newAdmins the admins going into the game
     */
    protected void start(@NotNull Collection<Team> newTeams, @NotNull Collection<Participant> newParticipants, @NotNull List<Player> newAdmins) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        listeners.forEach(listener -> listener.register(plugin));
        for (Team newTeam : newTeams) {
            T team = createTeam(newTeam);
            teams.put(team.getTeamId(), team);
            setupTeamOptions(team);
            tabList.addTeam(team.getTeamId(), team.getDisplayName(), team.getColor());
            initializeTeam(team);
        }
        for (Participant newParticipant : newParticipants) {
            P participant = createParticipant(newParticipant);
            T team = teams.get(participant.getTeamId());
            tabList.joinParticipant(participant.getParticipantID(), participant.getName(), participant.getTeamId(), false);
            addParticipant(participant, team);
            participant.setGameMode(GameMode.ADVENTURE);
            ParticipantInitializer.clearStatusEffects(participant);
            ParticipantInitializer.clearInventory(participant);
            ParticipantInitializer.resetHealthAndHunger(participant);
            initializeParticipant(participant, team);
        }
        _initializeSidebar();
        
        // admin start
        for (Player admin : newAdmins) {
            _initializeAdmin(admin);
        }
        _initializeAdminSidebar();
        // admin end
        this.state = getStartState();
    }
    
    /**
     * Add a new {@link UIManager} to this game
     * @param uiManager the {@link UIManager} to add
     * @return the newly added manager, for convenience
     * @param <U> the type of UIManager you input (so that it can be returned
     *           as the same type).
     */
    protected <U extends UIManager> U addUIManager(U uiManager) {
        this.uiManagers.add(uiManager);
        return uiManager;
    }
    
    /**
     * <p>This will be assigned to {@link #state} at the end of 
     * {@link #start(Collection, Collection, List)}. This state should kick off the game loop.</p>
     * @return the state to be instantiated after initialization
     */
    protected abstract @NotNull S getStartState();
    
    /**
     * @param state assign a state to this game
     */
    public void setState(@NotNull S state) {
        this.state = state;
    }
    
    // cleanup start
    @Override
    public void stop() {
        HandlerList.unregisterAll(this);
        listeners.forEach(GameListener::unregister);
        listeners.clear();
        timerManager.cancel();
        state.cleanup();
        Map<String, Integer> teamScores = getTeamScores();
        Map<UUID, Integer> participantScores = getParticipantScores();
        for (P participant : participants.values()) {
            participant.setGameMode(GameMode.SPECTATOR);
            _resetParticipant(participant, teams.get(participant.getTeamId()));
        }
        quitDatas.clear();
        teams.clear();
        teamQuitDatas.clear();
        // admins start
        for (Player admin : admins) {
            _resetAdmin(admin);
        }
        uiManagers.forEach(UIManager::cleanup);
        uiManagers.clear();
        adminSidebar.deleteAllLines();
        // admins end
        sidebar.deleteAllLines();
        tabList.cleanup();
        for (StoredGameRule<?> stored : storedGameRules) {
            restoreGameRules(stored);
        }
        storedGameRules.clear();
        cleanup();
        gameManager.gameIsOver(getGameInstanceId(), teamScores, participantScores, participants.values().stream().map(Participant::getUniqueId).toList(), admins);
        participants.clear();
        admins.clear();
        Main.logger().info("Stopping " + type.getTitle());
    }
    
    /**
     * Because of the {@code ?} used in {@link #storedGameRules}, this
     * method helps tell the compiler that {@link StoredGameRule#rule()}'s
     * type parameter and {@link StoredGameRule#value()} are the same type.
     * @param stored the stored rule
     * @param <G> the type of the rule's type parameter and value.
     */
    protected <G> void restoreGameRules(StoredGameRule<G> stored) {
        getWorld().setGameRule(stored.rule(), stored.value());
    }
    
    protected Map<String, Integer> getTeamScores() {
        Map<String, Integer> teamScores = new HashMap<>();
        for (String teamId : teamQuitDatas.keySet()) {
            teamScores.put(teamId, teamQuitDatas.get(teamId).getScore());
        }
        for (T team : teams.values()) {
            teamScores.put(team.getTeamId(), team.getScore());
        }
        return teamScores;
    }
    
    protected Map<UUID, Integer> getParticipantScores() {
        Map<UUID, Integer> participantScores = new HashMap<>();
        for (ParticipantID pid : quitDatas.keySet()) {
            participantScores.put(pid.uuid(), quitDatas.get(pid).getScore());
        }
        for (P participant : participants.values()) {
            participantScores.put(participant.getUniqueId(), participant.getScore());
        }
        return participantScores;
    }
    
    /**
     * <p>Cleanup tasks for the end of the game</p>
     */
    protected abstract void cleanup();
    // cleanup end
    
    // Participant start
    /**
     * <p>Add the participant to the game, to their team, and to UI managers</p>
     * @param participant the participant to add
     * @param team the team to add the participant to
     */
    protected void addParticipant(P participant, T team) {
        participants.put(participant.getUniqueId(), participant);
        team.addParticipant(participant);
        sidebar.addPlayer(participant);
        tabList.showPlayer(participant);
        uiManagers.forEach(uiManager -> uiManager.showPlayer(participant));
    }
    
    /**
     * <p>Create a participant from the given {@link Participant}.</p>
     * <p>Called after setting the participant to the defaults.
     * Add additional setup logic here for every time a participant is created.</p>
     *
     * @param participant the participant from which to derive the {@link P} type participant
     * @return the created {@link P} participant
     */
    protected abstract @NotNull P createParticipant(Participant participant);
    
    /**
     * <p>Create a participant from the given {@link Participant} and {@link QP} quitData.</p>
     * <p>Called after setting the participant to the defaults.
     * Add additional setup logic here for every time a participant is created, with the given quitData.</p>
     *
     * @param participant the participant from which to derive the {@link P} type participant
     * @param quitData    the quitData to use in creating the participant
     * @return the created {@link P} participant
     */
    protected abstract @NotNull P createParticipant(Participant participant, QP quitData);
    
    /**
     * Create quitData from the given participant
     *
     * @param participant the participant to get the quitData from
     * @return a new {@link QP} quitData from the given {@link P} participant's data
     */
    protected abstract @NotNull QP getQuitData(P participant);
    
    /**
     * <p>Prepare the participant for the game during initialization.</p>
     * <p>This is for any game-specific preparations, called once in the constructor.</p>
     * @param participant the participant
     * @param team the participant's team
     */
    protected abstract void initializeParticipant(P participant, T team);
    
    /**
     * <p>Prepare the team for the game during initialization.</p>
     * <p>This is called before participants are added to their teams, once in the constructor.</p>
     * @param team the team
     */
    protected abstract void initializeTeam(T team);
    
    /**
     * Create a new team of type {@link T} from the given {@link Team}
     *
     * @param team the team from which to derive the {@link T} type team
     * @return the created {@link T} team
     */
    protected abstract @NotNull T createTeam(Team team);
    
    /**
     * Create a new team of type {@link T} from the given {@link Team} and {@link QT} quitData
     *
     * @param team     the team from which to derive the {@link T} type team
     * @param quitData the quitData to use in creating the team
     * @return the created {@link T} team
     */
    protected abstract @NotNull T createTeam(Team team, QT quitData);
    
    /**
     * Create quitData from the given team
     *
     * @param team the team to get the quitData from
     * @return a new {@link QT} quitData from the given {@link T} team's data
     */
    protected abstract @NotNull QT getQuitData(T team);
    
    protected void _resetParticipant(P participant, T team) {
        team.removeParticipant(participant.getUniqueId());
        ParticipantInitializer.clearInventory(participant);
        ParticipantInitializer.resetHealthAndHunger(participant);
        ParticipantInitializer.clearStatusEffects(participant);
        sidebar.removePlayer(participant);
        tabList.hidePlayer(participant);
        uiManagers.forEach(uiManager -> uiManager.hidePlayer(participant));
        resetParticipant(participant, team);
    }
    
    /**
     * <p>Reset the participant, removing all game-specific state</p>
     * <p>This is called after default reset behavior, 
     * and is only needed for implementation-specific reset behavior.</p>
     * @param participant the participant to reset
     * @param team the participant's team
     */
    protected abstract void resetParticipant(P participant, T team);
    
    /**
     * @param uuid the UUID of the participant to get
     * @return the participant with the given UUID (or null if no participant exists)
     */
    public @Nullable P getParticipant(@NotNull UUID uuid) {
        return participants.get(uuid);
    }
    
    protected void setupTeamOptions(T team) {
        org.bukkit.scoreboard.Team scoreboardTeam = gameManager.getMctScoreboard().getTeam(team.getTeamId());
        if (scoreboardTeam != null) {
            setupTeamOptions(scoreboardTeam, team);
        } else {
            Main.logger().log(Level.SEVERE,
                    String.format("MCT scoreboard could not find team with id %s", team.getTeamId()),
                    new IllegalStateException(
                            String.format("Scoreboard does not contain teamId %s", team.getTeamId())));
        }
    }
    
    /**
     * Reset the scoreboard options to defaults for the given team
     * @param team the team
     */
    protected void resetTeamOptions(@NotNull T team) {
        org.bukkit.scoreboard.Team scoreboardTeam = gameManager.getMctScoreboard().getTeam(team.getTeamId());
        if (scoreboardTeam != null) {
            scoreboardTeam.setAllowFriendlyFire(false);
            scoreboardTeam.setCanSeeFriendlyInvisibles(true);
            scoreboardTeam.setOption(org.bukkit.scoreboard.Team.Option.NAME_TAG_VISIBILITY, org.bukkit.scoreboard.Team.OptionStatus.ALWAYS);
            scoreboardTeam.setOption(org.bukkit.scoreboard.Team.Option.DEATH_MESSAGE_VISIBILITY, org.bukkit.scoreboard.Team.OptionStatus.ALWAYS);
            scoreboardTeam.setOption(org.bukkit.scoreboard.Team.Option.COLLISION_RULE, org.bukkit.scoreboard.Team.OptionStatus.NEVER);
        } else {
            Main.logger().log(Level.SEVERE,
                    String.format("MCT scoreboard could not find team with id %s", team.getTeamId()),
                    new IllegalStateException(
                            String.format("Scoreboard does not contain teamId %s", team.getTeamId())));
        }
    }
    
    /**
     * Set up the scoreboard options for the given team, such as collisions
     * @param scoreboardTeam the scoreboard team to set the options for
     * @param team the team representing the options team
     */
    protected abstract void setupTeamOptions(@NotNull org.bukkit.scoreboard.Team scoreboardTeam, @NotNull T team);
    // Participant end
    
    // quit/join start
    @Override
    public void onTeamJoin(Team newTeam) {
        if (teams.containsKey(newTeam.getTeamId())) {
            return;
        }
        QT quitTeam = teamQuitDatas.remove(newTeam.getTeamId());
        if (quitTeam != null) {
            T team = createTeam(newTeam, quitTeam);
            teams.put(team.getTeamId(), team);
            setupTeamOptions(team);
            state.onTeamRejoin(team);
        } else {
            T team = createTeam(newTeam);
            teams.put(team.getTeamId(), team);
            setupTeamOptions(team);
            tabList.addTeam(team.getTeamId(), team.getDisplayName(), team.getColor());
            state.onNewTeamJoin(team);
        }
    }
    
    @Override
    public void onParticipantJoin(Participant newParticipant) {
        T team = teams.get(newParticipant.getTeamId());
        QP quitData = quitDatas.remove(newParticipant.getParticipantID());
        newParticipant.setGameMode(GameMode.ADVENTURE);
        ParticipantInitializer.clearStatusEffects(newParticipant);
        ParticipantInitializer.clearInventory(newParticipant);
        ParticipantInitializer.resetHealthAndHunger(newParticipant);
        P participant;
        if (quitData != null) {
            participant = createParticipant(newParticipant, quitData);
            tabList.setParticipantGrey(participant.getParticipantID(), false);
            addParticipant(participant, team);
            state.onParticipantRejoin(participant, team);
        } else {
            participant = createParticipant(newParticipant);
            tabList.joinParticipant(participant.getParticipantID(), participant.getName(), participant.getTeamId(), false);
            addParticipant(participant, team);
            state.onNewParticipantJoin(participant, team);
        }
        // update the UI
        sidebar.updateLine(participant.getUniqueId(), "title", title);
        displayScore(participant);
        displayScore(team);
    }
    
    @Override
    public void onParticipantQuit(UUID participantUUID) {
        P participant = participants.get(participantUUID);
        if (participant == null) {
            return;
        }
        T team = teams.get(participant.getTeamId());
        state.onParticipantQuit(participant, team);
        participants.remove(participantUUID);
        team.removeParticipant(participantUUID);
        quitDatas.put(participant.getParticipantID(), getQuitData(participant));
        tabList.setParticipantGrey(participant.getParticipantID(), true);
        participant.setGameMode(GameMode.ADVENTURE);
        _resetParticipant(participant, team);
    }
    
    @Override
    public void onTeamQuit(@NotNull String teamId) {
        T team = teams.get(teamId);
        if (team == null || team.size() > 0) {
            return;
        }
        state.onTeamQuit(team);
        teams.remove(team.getTeamId());
        teamQuitDatas.put(team.getTeamId(), getQuitData(team));
        resetTeamOptions(team);
        if (teams.isEmpty()) {
            stop();
        }
    }
    // quit/join end
    
    /**
     * @param title the new title to display on the sidebar
     */
    @Override
    public void setTitle(@NotNull Component title) {
        this.title = title;
        sidebar.updateLine("title", title);
        adminSidebar.updateLine("title", title);
    }
    
    // admin start
    protected void _initializeAdmin(Player admin) {
        admins.add(admin);
        adminSidebar.addPlayer(admin);
        tabList.showPlayer(admin);
        admin.setGameMode(GameMode.SPECTATOR);
        uiManagers.forEach(uiManager -> uiManager.showPlayer(admin));
        initializeAdmin(admin);
    }
    
    /**
     * <p>Prepare the admin for the game</p>
     * <p>This is called after default initialization, and is only needed for
     * implementation-specific initialization.</p>
     * @param admin the admin
     */
    protected abstract void initializeAdmin(Player admin);
    
    protected void _initializeAdminSidebar() {
        adminSidebar.addLines(
                new KeyLine("title", title)
        );
        initializeAdminSidebar();
    }
    
    /**
     * <p>Add custom lines to {@link #adminSidebar}</p>
     */
    protected abstract void initializeAdminSidebar();
    
    protected void _resetAdmin(Player admin) {
        uiManagers.forEach(uiManager -> uiManager.hidePlayer(admin));
        adminSidebar.removePlayer(admin);
        tabList.hidePlayer(admin);
        resetAdmin(admin);
    }
    
    /**
     * <p>Reset the admin</p>
     * @param admin the admin to reset
     */
    protected abstract void resetAdmin(Player admin);
    
    @Override
    public void onAdminJoin(Player admin) {
        _initializeAdmin(admin);
        adminSidebar.updateLine(admin.getUniqueId(), "title", title);
    }
    
    @Override
    public void onAdminQuit(Player admin) {
        _resetAdmin(admin);
        admins.remove(admin);
    }
    // admin end
    
    // Sidebar start
    /**
     * Add the appropriate default lines to the sidebar
     * and display the participant and team scores
     */
    protected void _initializeSidebar() {
        sidebar.addLines(
                new KeyLine("personalTeam", ""),
                new KeyLine("personalScore", ""),
                new KeyLine("title", title)
        );
        initializeSidebar();
        for (T team : teams.values()) {
            displayScore(team);
        }
        for (P participant : participants.values()) {
            displayScore(participant);
        }
    }
    
    /**
     * <p>Add custom lines to the {@link #sidebar}</p>
     * <p>Called after initial lines are added 
     * and before team and participant scores are initially displayed.</p>
     */
    protected abstract void initializeSidebar();
    
    /**
     * Display the score of the given team on the {@link #sidebar}s of all the
     * team's members.
     * @param team the team to display the score of. Uses {@link T#getScore()}.
     */
    public void displayScore(T team) {
        Component contents = Component.empty()
                .append(team.getFormattedDisplayName())
                .append(Component.text(": "))
                .append(Component.text(team.getScore())
                        .color(NamedTextColor.GOLD));
        for (UUID memberUUID : team.getMemberUUIDs()) {
            sidebar.updateLine(memberUUID, "personalTeam", contents);
        }
        tabList.setScore(team.getTeamId(), team.getScore());
    }
    
    /**
     * Display the scores of the given teams on the {@link #sidebar}s of all the
     * team's members.
     * @param theTeams the teams to display the scores of. Uses {@link T#getScore()}.
     */
    public final void displayTeamScores(Collection<T> theTeams) {
        for (T team : theTeams) {
            Component contents = Component.empty()
                    .append(team.getFormattedDisplayName())
                    .append(Component.text(": "))
                    .append(Component.text(team.getScore())
                            .color(NamedTextColor.GOLD));
            for (UUID memberUUID : team.getMemberUUIDs()) {
                sidebar.updateLine(memberUUID, "personalTeam", contents);
            }
        }
        tabList.setScores(theTeams);
    }
    
    /**
     * Display the score of the given participant on their personal {@link #sidebar}.
     * @param participant the participant to display the score of. Uses {@link P#getScore()}.
     */
    public void displayScore(P participant) {
        sidebar.updateLine(participant.getUniqueId(), "personalScore", Component.empty()
                .append(Component.text("Personal: "))
                .append(Component.text(participant.getScore()))
                .color(NamedTextColor.GOLD));
    }
    
    /**
     * Display the scores of the given participants on their personal {@link #sidebar}s
     * @param theParticipants the participants to display the scores of. Uses {@link P#getScore()}
     */
    public void displayParticipantScores(Collection<P> theParticipants) {
        for (P participant : theParticipants) {
            displayScore(participant);
        }
    }
    // Sidebar end
    
    // Award Points start
    
    /**
     * <p>Award the given points to the given participant. The points will be multiplied by 
     * {@link GameManager#getMultiplier()} before being awarded, and points will be reflected 
     * in the participant's team as well.</p>
     * <p>{@link #sidebar} will also be updated to reflect the score. </p>
     * @param participant the participant to be awarded personal points
     * @param points the points to be awarded (un-multiplied, base points)
     */
    public void awardPoints(P participant, int points) {
        int multiplied = (int) (points * gameManager.getMultiplier());
        participant.awardPoints(multiplied);
        T team = teams.get(participant.getTeamId());
        team.addPoints(multiplied);
        displayScore(participant);
        displayScore(team);
    }
    
    /**
     * <p>Award the given points to the given team. The points will be multiplied by 
     * {@link GameManager#getMultiplier()} before being awarded.</p>
     * <p>{@link #sidebar} will also be updated to reflect the score</p>
     * @param team the team to award the points to
     * @param points the points to be awarded (un-multiplied, base points)
     */
    public void awardPoints(T team, int points) {
        int multiplied = (int) (points * gameManager.getMultiplier());
        team.awardPoints(multiplied);
        displayScore(team);
    }
    
    /**
     * <p>Award the given points to the given participants. The points will be multiplied by 
     * {@link GameManager#getMultiplier()} before being awarded, and points will be reflected 
     * in the participants' team as well.</p>
     * <p>{@link #sidebar} will also be updated to reflect the score. </p>
     * @param awardedParticipants the participants to be awarded personal points
     * @param points the points to be awarded (un-multiplied, base points)
     */
    public void awardParticipantPoints(Collection<P> awardedParticipants, int points) {
        int multiplied = (int) (points * gameManager.getMultiplier());
        Set<T> awardedTeams = new HashSet<>();
        for (P participant : awardedParticipants) {
            participant.awardPoints(multiplied);
            T team = teams.get(participant.getTeamId());
            team.addPoints(multiplied);
            awardedTeams.add(team);
        }
        displayParticipantScores(awardedParticipants);
        displayTeamScores(awardedTeams);
    }
    
    /**
     * <p>Award the given points to the given teams. The points will be multiplied by 
     * {@link GameManager#getMultiplier()} before being awarded.</p>
     * <p>{@link #sidebar} will also be updated to reflect the score</p>
     * @param awardedTeams the teams to award the points to
     * @param points the points to be awarded (un-multiplied, base points)
     */
    public void awardTeamPoints(Collection<T> awardedTeams, int points) {
        int multiplied = (int) (points * gameManager.getMultiplier());
        for (T team : awardedTeams) {
            team.awardPoints(multiplied);
        }
        displayTeamScores(awardedTeams);
    }
    // Award Points end
    
    // EventHandlers start
    /**
     * <p>The default behavior for {@link PlayerMoveEvent}s. Checks if the triggering player is 
     * a participant in this game, and if so passes the event (and the participant) to the 
     * {@link GameStateBase#onParticipantMove(PlayerMoveEvent, ParticipantData)}.</p>
     * <p>After the state handles the move event, if the event isn't cancelled and 
     * {@link #getSpectatorBoundary()} is not null, keeps participants in spectator 
     * mode inside the specified boundary.</p>
     * @param event the event
     */
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        P participant = participants.get(event.getPlayer().getUniqueId());
        if (participant == null) {
            return;
        }
        state.onParticipantMove(event, participant);
        SpectatorBoundary boundary = getSpectatorBoundary();
        if (boundary != null &&
                !event.isCancelled() && 
                participant.getGameMode().equals(GameMode.SPECTATOR) &&
                !participant.getPlayer().isDead() &&
                boundary.contains(event.getFrom().toVector()) &&
                !boundary.contains(event.getTo().toVector())) {
            event.setCancelled(true);
        }
    }
    
    /**
     * @return the {@link SpectatorBoundary} to keep spectators in
     */
    protected abstract @Nullable SpectatorBoundary getSpectatorBoundary();
    
    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        P participant = participants.get(event.getPlayer().getUniqueId());
        if (participant == null) {
            return;
        }
        state.onParticipantTeleport(event, participant);
        if (getSpectatorBoundary() != null &&
                !event.isCancelled() &&
                event.getCause().equals(PlayerTeleportEvent.TeleportCause.SPECTATE) &&
                participant.getGameMode().equals(GameMode.SPECTATOR) &&
                !participant.getPlayer().isDead() &&
                !getSpectatorBoundary().contains(event.getTo().toVector())) {
            event.setCancelled(true);
        }
    }
    
    /**
     * <p>Default behavior for {@link PlayerInteractEvent}. 
     * First checks to see if the triggering player is a participant in this game.
     * Then, checks to see if interactions with any interacted blocks 
     * should be prevented (see 
     * {@link #shouldPreventInteractions(Material)}) and if so, 
     * cancels the event and does not pass to the implementing game class. 
     * Otherwise, the event and participant is passed to 
     * {@link GameStateBase#onParticipantInteract(PlayerInteractEvent, P)}.</p>
     * @param event the event
     */
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        P participant = participants.get(event.getPlayer().getUniqueId());
        if (participant == null) {
            return;
        }
        Block clickedBlock = event.getClickedBlock();
        if (clickedBlock != null && shouldPreventInteractions(clickedBlock.getType())) {
            event.setUseInteractedBlock(Event.Result.DENY);
        }
        state.onParticipantInteract(event, participant);
    }
    
    /**
     * @param type the block type to check if it should be interacted with in this game
     * @return true if interactions with the given block type should be prevented, false if they should be allowed. 
     */
    protected abstract boolean shouldPreventInteractions(@NotNull Material type);
    
    /**
     * <p>Default behavior for {@link EntityDamageEvent}. If the entity is a participant in 
     * this game, the event and participant is passed to 
     * {@link GameStateBase#onParticipantDamage(EntityDamageEvent, P)}. 
     * Otherwise it is ignored.</p>
     * @param event the event
     */
    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (GameManagerUtils.EXCLUDED_DAMAGE_CAUSES.contains(event.getCause())) {
            return;
        }
        P participant = participants.get(event.getEntity().getUniqueId());
        if (participant == null) {
            return;
        }
        state.onParticipantDamage(event, participant);
    }
    
    /**
     * <p>Default behavior for {@link PlayerDeathEvent}. If the entity is a participant in 
     * this game, the event and participant is passed to 
     * {@link GameStateBase#onParticipantDeath(PlayerDeathEvent, P)}. 
     * Otherwise it is ignored.</p>
     * @param event the event
     */
    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        P participant = participants.get(event.getPlayer().getUniqueId());
        if (participant == null) {
            return;
        }
        state.onParticipantDeath(event, participant);
    }
    
    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        P participant = participants.get(event.getPlayer().getUniqueId());
        if (participant == null) {
            return;
        }
        state.onParticipantRespawn(event, participant);
    }
    
    @EventHandler
    public void onPlayerPostRespawn(PlayerPostRespawnEvent event) {
        P participant = participants.get(event.getPlayer().getUniqueId());
        if (participant == null) {
            return;
        }
        state.onParticipantPostRespawn(event, participant);
    }
    // EventHandlers end
    
    // commands start
    @Override
    public @NotNull CommandResult top(@NotNull UUID uuid) {
        P participant = participants.get(uuid);
        if (participant == null) {
            return CommandResult.failure(Component.empty()
                    .append(Component.text("Not a participant in "))
                    .append(Component.text(getType().getTitle())));
        }
        return state.top(participant);
    }
    // commands end
    
    /**
     * Convenience method to send the same message to all participants and admins
     * @param message the message to send
     */
    public void messageAllParticipants(@NotNull Component message) {
        Audience.audience(
                Audience.audience(admins),
                Audience.audience(participants.values())
        ).sendMessage(message);
    }
    
    /**
     * Convenience method to send the same title to all participants and admins
     * @param title the title to send
     */
    public void titleAllParticipants(@NotNull Title title) {
        Audience.audience(
                Audience.audience(admins),
                Audience.audience(participants.values())
        ).showTitle(title);
    }
}
