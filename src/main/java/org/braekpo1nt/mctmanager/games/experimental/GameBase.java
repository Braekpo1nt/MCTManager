package org.braekpo1nt.mctmanager.games.experimental;

import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.braekpo1nt.mctmanager.games.game.enums.GameType;
import org.braekpo1nt.mctmanager.games.game.interfaces.MCTGame;
import org.braekpo1nt.mctmanager.games.utils.ParticipantInitializer;
import org.braekpo1nt.mctmanager.participant.*;
import org.braekpo1nt.mctmanager.ui.UIManager;
import org.braekpo1nt.mctmanager.ui.sidebar.KeyLine;
import org.braekpo1nt.mctmanager.ui.sidebar.Sidebar;
import org.braekpo1nt.mctmanager.ui.timer.TimerManager;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * @param <P> the ParticipantData implementation used by this game
 * @param <T> the ScoredTeamData implementation used by this game
 */
@Getter
@Setter
// TODO: this could be simplified by making QT and QP be T and P instead. In other words, quitDatas should be Map<UUID, P> quitParticipants, and teamQuitDatas should be Map<String, T> quitTeams. GameStateBase would need to change to make sure you are not re-using the previously stored Player object from when the player quit. 
// TODO: move GameData to GameStateBase? 
public abstract class GameBase<P extends ParticipantData, T extends ScoredTeamData<P>, QP extends QuitDataBase, QT extends QuitDataBase, S extends GameStateBase<P, T>>  implements MCTGame, Listener, GameData<P> {
    protected final @NotNull GameType type;
    protected final @NotNull Main plugin;
    protected final @NotNull GameManager gameManager;
    protected final @NotNull TimerManager timerManager;
    protected final @NotNull Sidebar sidebar;
    protected final @NotNull Sidebar adminSidebar;
    protected final @NotNull Map<UUID, P> participants;
    protected final @NotNull Map<ParticipantID, QP> quitDatas;
    protected final @NotNull Map<String, T> teams;
    protected final @NotNull Map<String, QT> teamQuitDatas;
    protected final @NotNull List<Player> admins;
    protected final @NotNull List<UIManager> uiManagers;
    protected final @NotNull List<GameListener<P>> listeners;
    
    /**
     * The current state of this game
     */
    protected @NotNull S state;
    protected @NotNull Component title;
    
    /**
     * Initialize data and start the game
     * @param type the type associated with this game
     * @param plugin the plugin
     * @param gameManager the GameManager
     * @param title the game's initial title, displayed in the sidebar
     */
    public GameBase(
            @NotNull GameType type, 
            @NotNull Main plugin,
            @NotNull GameManager gameManager,
            @NotNull Component title) {
        this.type = type;
        this.plugin = plugin;
        this.gameManager = gameManager;
        this.sidebar = gameManager.createSidebar();
        this.adminSidebar = gameManager.createSidebar();
        this.participants = new HashMap<>();
        this.quitDatas = new HashMap<>();
        this.teams = new HashMap<>();
        this.teamQuitDatas = new HashMap<>();
        this.title = title;
        this.uiManagers = new ArrayList<>();
        this.timerManager = gameManager.getTimerManager().register(new TimerManager(plugin));
        this.admins = new ArrayList<>();
        this.listeners = new ArrayList<>();
    }
    
    protected void addListener(GameListener<P> listener) {
        this.listeners.add(listener);
    }
    
    /**
     * Start the game after all fields have been initialized, and instantiate the initial state.
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
            initializeTeam(team);
        }
        for (Participant newParticipant : newParticipants) {
            P participant = createParticipant(newParticipant);
            T team = teams.get(participant.getTeamId());
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
    
    protected <U extends UIManager> U addUIManager(U uiManager) {
        this.uiManagers.add(uiManager);
        return uiManager;
    }
    
    /**
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
        _cancelAllTasks();
        state.cleanup();
        saveScores();
        for (P participant : participants.values()) {
            _resetParticipant(participant, teams.get(participant.getTeamId()));
        }
        participants.clear();
        quitDatas.clear();
        teams.clear();
        teamQuitDatas.clear();
        // admins start
        for (Player admin : admins) {
            _resetAdmin(admin);
        }
        adminSidebar.deleteAllLines();
        admins.clear();
        // admins end
        sidebar.deleteAllLines();
        cleanup();
        gameManager.gameIsOver();
        Main.logger().info("Stopping " + type.getTitle());
    }
    
    private void saveScores() {
        Map<String, Integer> teamScores = new HashMap<>();
        Map<UUID, Integer> participantScores = new HashMap<>();
        for (T team : teams.values()) {
            teamScores.put(team.getTeamId(), team.getScore());
        }
        for (P participant : participants.values()) {
            participantScores.put(participant.getUniqueId(), participant.getScore());
        }
        for (String teamId : teamQuitDatas.keySet()) {
            teamScores.put(teamId, teamQuitDatas.get(teamId).getScore());
        }
        for (ParticipantID pid : quitDatas.keySet()) {
            participantScores.put(pid.uuid(), quitDatas.get(pid).getScore());
        }
        gameManager.addScores(teamScores, participantScores);
    }
    
    private void _cancelAllTasks() {
        timerManager.cancel();
        cancelAllTasks();
    }
    
    /**
     * <p>Cancel any scheduled tasks, such as {@link org.bukkit.scheduler.BukkitTask}s</p>
     */
    protected abstract void cancelAllTasks();
    
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
        uiManagers.forEach(uiManager -> uiManager.showPlayer(participant));
    }
    
    /**
     * <p>Create a participant from the given {@link Participant} and {@link QP} quitData.</p>
     * <p>Called after setting the participant to the defaults. 
     * Add additional setup logic here for every time a participant is created, with the given quitData.</p>
     * @param participant the participant from which to derive the {@link P} type participant
     * @param quitData the quitData to use in creating the participant
     * @return the created {@link P} participant
     */
    protected abstract P createParticipant(Participant participant, QP quitData);
    
    /**
     * <p>Create a participant from the given {@link Participant}.</p>
     * <p>Called after setting the participant to the defaults.
     * Add additional setup logic here for every time a participant is created.</p>
     *
     * @param fromParticipant the participant from which to derive the {@link P} type participant
     * @return the created {@link P} participant
     */
    protected abstract P createParticipant(Participant fromParticipant);
    
    /**
     * Create quitData from the given participant
     * @param participant the participant to get the quitData from
     * @return a new {@link QP} quitData from the given {@link P} participant's data
     */
    protected abstract QP getQuitData(P participant);
    
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
     * Create a new team of type {@link T} from the given {@link Team} and {@link QT} quitData
     *
     * @param team     the team from which to derive the {@link T} type team
     * @param quitData the quitData to use in creating the team
     * @return the created {@link T} team
     */
    protected abstract T createTeam(Team team, QT quitData);
    
    /**
     * Create a new team of type {@link T} from the given {@link Team}
     *
     * @param team the team from which to derive the {@link T} type team
     * @return the created {@link T} team
     */
    protected abstract T createTeam(Team team);
    
    /**
     * Create quitData from the given team
     * @param team the team to get the quitData from
     * @return a new {@link QT} quitData from the given {@link T} team's data
     */
    protected abstract QT getQuitData(T team);
    
    protected void _resetParticipant(P participant, T team) {
        team.removeParticipant(participant.getUniqueId());
        ParticipantInitializer.clearInventory(participant);
        ParticipantInitializer.resetHealthAndHunger(participant);
        ParticipantInitializer.clearStatusEffects(participant);
        participant.setGameMode(GameMode.SPECTATOR);
        sidebar.removePlayer(participant);
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
    // Participant end
    
    // quit/join start
    
    /**
     * {@inheritDoc}
     */
    @Deprecated
    @Override
    public void onParticipantJoin(Participant participant, Team team) {
        onTeamJoin(team);
        onParticipantJoin(participant);
    }
    
    /**
     * {@inheritDoc}
     */
    @Deprecated
    @Override
    public void onParticipantQuit(UUID participantUUID, String teamId) {
        onParticipantQuit(participantUUID);
        onTeamQuit(teamId);
    }
    
    @Override
    public void onTeamJoin(Team newTeam) {
        if (teams.containsKey(newTeam.getTeamId())) {
            return;
        }
        QT quitTeam = teamQuitDatas.remove(newTeam.getTeamId());
        if (quitTeam != null) {
            T team = createTeam(newTeam, quitTeam);
            teams.put(team.getTeamId(), team);
            state.onTeamRejoin(team);
        } else {
            T team = createTeam(newTeam);
            teams.put(team.getTeamId(), team);
            state.onNewTeamJoin(team);
        }
    }
    
    @Override
    public void onParticipantJoin(Participant newParticipant) {
        T team = teams.get(newParticipant.getTeamId());
        QP quitData = quitDatas.get(newParticipant.getParticipantID());
        newParticipant.setGameMode(GameMode.ADVENTURE);
        ParticipantInitializer.clearStatusEffects(newParticipant);
        ParticipantInitializer.clearInventory(newParticipant);
        ParticipantInitializer.resetHealthAndHunger(newParticipant);
        P participant;
        if (quitData != null) {
            participant = createParticipant(newParticipant, quitData);
            addParticipant(participant, team);
            state.onParticipantRejoin(participant, team);
        } else {
            participant = createParticipant(newParticipant);
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
        P participant = participants.remove(participantUUID);
        if (participant == null) {
            return;
        }
        T team = teams.get(participant.getTeamId());
        state.onParticipantQuit(participant, team);
        quitDatas.put(participant.getParticipantID(), getQuitData(participant));
        _resetParticipant(participant, team);
    }
    
    @Override
    public void onTeamQuit(String teamId) {
        T team = teams.get(teamId);
        if (team.size() > 0) {
            return;
        }
        state.onTeamQuit(team);
        teams.remove(team.getTeamId());
        teamQuitDatas.put(team.getTeamId(), getQuitData(team));
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
    private void _initializeAdmin(Player admin) {
        admins.add(admin);
        adminSidebar.addPlayer(admin);
        admin.setGameMode(GameMode.SPECTATOR);
        initializeAdmin(admin);
    }
    
    /**
     * <p>Prepare the admin for the game</p>
     * <p>This is called after default initialization, and is only needed for
     * implementation-specific initialization.</p>
     * @param admin the admin
     */
    protected abstract void initializeAdmin(Player admin);
    
    private void _initializeAdminSidebar() {
        adminSidebar.addLines(
                new KeyLine("title", title)
        );
        initializeAdminSidebar();
    }
    
    /**
     * <p>Add custom lines to {@link #adminSidebar}</p>
     */
    protected abstract void initializeAdminSidebar();
    
    private void _resetAdmin(Player admin) {
        adminSidebar.removePlayer(admin);
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
    // Sidebar end
    
}
