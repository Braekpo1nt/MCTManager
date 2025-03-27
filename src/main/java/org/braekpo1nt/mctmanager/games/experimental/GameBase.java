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
// TODO: this could be simplified by making QT and QP be T and P instead. In other words, quitDatas should be Map<UUID, P> quitParticipants, and teamQuitDatas should be Map<String, T> quitTeams. GameStateBase would need to change to make sure you are not re-using the previously stored Player object from when the player quit. Also take this opportunity to differentiate between players rejoining on the same or a different team from that which they quit as a member of. 
public abstract class GameBase<P extends ParticipantData, T extends ScoredTeamData<P>, QP extends QuitDataBase, QT extends QuitDataBase>  implements MCTGame, Listener {
    protected final @NotNull GameType type;
    protected final @NotNull Main plugin;
    protected final @NotNull GameManager gameManager;
    protected final @NotNull TimerManager timerManager;
    protected final @NotNull Sidebar sidebar;
    protected final @NotNull Sidebar adminSidebar;
    protected final @NotNull Map<UUID, P> participants;
    protected final @NotNull Map<UUID, QP> quitDatas;
    protected final @NotNull Map<String, T> teams;
    protected final @NotNull Map<String, QT> teamQuitDatas;
    protected final @NotNull List<Player> admins;
    protected final @NotNull List<UIManager> uiManagers;
    
    /**
     * The current state of this game
     */
    protected @NotNull GameStateBase<P, T, QP, QT> state;
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
        this.state = new EmptyState(this);
    }
    
    /**
     * Start the game after all fields have been initialized, and instantiate the initial state.
     * @param newTeams the teams going into the game
     * @param newParticipants the participants going into the game
     * @param newAdmins the admins going into the game
     */
    protected void start(@NotNull Collection<Team> newTeams, @NotNull Collection<Participant> newParticipants, @NotNull List<Player> newAdmins) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        for (Team newTeam : newTeams) {
            T team = createTeam(newTeam);
            teams.put(team.getTeamId(), team);
        }
        for (Participant newParticipant : newParticipants) {
            _initializeParticipant(newParticipant);
        }
        _initializeSidebar();
        
        // admin start
        for (Player admin : newAdmins) {
            _initializeAdmin(admin);
        }
        _initializeAdminSidebar();
        // admin end
        this.state = getInitialState();
    }
    
    protected <U extends UIManager> U addUIManager(U uiManager) {
        this.uiManagers.add(uiManager);
        return uiManager;
    }
    
    /**
     * @return the first state to be assigned to {@link #state}
     */
    protected abstract GameStateBase<P, T, QP, QT> getInitialState();
    
    // cleanup start
    @Override
    public void stop() {
        HandlerList.unregisterAll(this);
        _cancelAllTasks();
        state.cleanup();
        preCleanup();
        saveScores();
        for (P participant : participants.values()) {
            _resetParticipant(participant);
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
        for (Map.Entry<String, QT> entry : teamQuitDatas.entrySet()) {
            teamScores.put(entry.getKey(), entry.getValue().getScore());
        }
        for (Map.Entry<UUID, QP> entry : quitDatas.entrySet()) {
            participantScores.put(entry.getKey(), entry.getValue().getScore());
        }
        gameManager.addScores(teamScores, participantScores);
    }
    
    private void _cancelAllTasks() {
        timerManager.cancel();
        cancelAllTasks();
    }
    
    /**
     * <p>Cancel any scheduled tasks or {@link org.bukkit.scheduler.BukkitTask}s</p>
     */
    protected abstract void cancelAllTasks();
    
    /**
     * <p>Cleanup tasks for the end of the game before 
     * the participants and teams are cleared</p>
     */
    protected void preCleanup() {
        // do nothing
    }
    
    /**
     * <p>Cleanup tasks for the end of the game</p>
     */
    protected abstract void cleanup();
    // cleanup end
    
    // Participant start
    private void _initializeParticipant(Participant newParticipant) {
        P participant = createParticipant(newParticipant);
        participants.put(participant.getUniqueId(), participant);
        sidebar.addPlayer(participant);
        uiManagers.forEach(uiManager -> uiManager.showPlayer(participant));
        T team = teams.get(participant.getTeamId());
        team.addParticipant(participant);
        participant.setGameMode(GameMode.ADVENTURE);
        ParticipantInitializer.clearStatusEffects(participant);
        ParticipantInitializer.clearInventory(participant);
        ParticipantInitializer.resetHealthAndHunger(participant);
        initializeParticipant(participant, team);
    }
    
    /**
     * Create a participant from the given {@link Participant} and {@link QP} quitData
     * @param participant the participant from which to derive the {@link P} type participant
     * @param quitData the quitData to use in creating the participant
     * @return the created {@link P} participant
     */
    protected abstract P createParticipant(Participant participant, QP quitData);
    
    /**
     * Create a participant from the given {@link Participant}
     * @param participant the participant from which to derive the {@link P} type participant
     * @return the created {@link P} participant
     */
    protected abstract P createParticipant(Participant participant);
    
    /**
     * Create quitData from the given participant
     * @param participant the participant to get the quitData from
     * @return a new {@link QP} quitData from the given {@link P} participant's data
     */
    public abstract QP getQuitData(P participant);
    
    /**
     * <p>Prepare the participant for the game</p>
     * <p>This is for any game-specific preparations</p>
     * @param participant the participant
     * @param team the participant's team
     */
    protected abstract void initializeParticipant(P participant, T team);
    
    /**
     * Create a new team of type {@link T} from the given {@link Team} and {@link QT} quitData
     *
     * @param team     the team from which to derive the {@link T} type team
     * @param quitData the quitData to use in creating the team
     * @return the created {@link T} team
     */
    public abstract T createTeam(Team team, QT quitData);
    
    /**
     * Create a new team of type {@link T} from the given {@link Team}
     *
     * @param team the team from which to derive the {@link T} type team
     * @return the created {@link T} team
     */
    public abstract T createTeam(Team team);
    
    /**
     * Create quitData from the given team
     * @param team the team to get the quitData from
     * @return a new {@link QT} quitData from the given {@link T} team's data
     */
    public abstract QT getQuitData(T team);
    
    private void _resetParticipant(P participant) {
        T team = teams.get(participant.getTeamId());
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
    
    
    @Override
    public void onParticipantJoin(Participant participant, Team team) {
        onTeamJoin(team);
        onParticipantJoin(participant);
    }
    
    @Override
    public void onParticipantQuit(UUID participantUUID, String teamId) {
        onParticipantQuit(participantUUID);
        onTeamQuit(teamId);
    }
    
    @Override
    public void onTeamJoin(Team team) {
        state.onTeamJoin(team);
    }
    
    @Override
    public void onParticipantJoin(Participant participant) {
        state.onParticipantJoin(participant);
    }
    
    @Override
    public void onParticipantQuit(UUID participantUUID) {
        state.onParticipantQuit(participantUUID);
    }
    
    @Override
    public void onTeamQuit(String teamId) {
        state.onTeamQuit(teamId);
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
    private void _initializeSidebar() {
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
     * Add custom lines to the {@link #sidebar}
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
    
    /**
     * Empty state which does nothing
     */
    private class EmptyState extends GameStateBase<P, T, QP, QT> {
        private final GameBase<P, T, QP, QT> context;
        
        public EmptyState(GameBase<P, T, QP, QT> context) {
            this.context = context;
        }
        
        @Override
        protected GameBase<P, T, QP, QT> getContext() {
            return context;
        }
        
        @Override
        public void cleanup() {
            
        }
        
        @Override
        protected void onTeamRejoin(T team) {
            
        }
        
        @Override
        protected void onNewTeamJoin(T team) {
            
        }
        
        @Override
        protected void onParticipantRejoin(P participant, T team) {
            
        }
        
        @Override
        protected void onNewParticipantJoin(P participant, T team) {
            
        }
        
        @Override
        protected void onParticipantQuit(P participant, T team) {
            
        }
        
        @Override
        protected void onTeamQuit(T team) {
            
        }
    }
    
}
