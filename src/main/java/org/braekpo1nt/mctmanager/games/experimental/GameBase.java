package org.braekpo1nt.mctmanager.games.experimental;

import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.braekpo1nt.mctmanager.games.game.capturetheflag.CTFParticipant;
import org.braekpo1nt.mctmanager.games.game.capturetheflag.CTFTeam;
import org.braekpo1nt.mctmanager.games.game.enums.GameType;
import org.braekpo1nt.mctmanager.games.game.interfaces.MCTGame;
import org.braekpo1nt.mctmanager.games.utils.ParticipantInitializer;
import org.braekpo1nt.mctmanager.participant.Participant;
import org.braekpo1nt.mctmanager.participant.ParticipantData;
import org.braekpo1nt.mctmanager.participant.ScoredTeamData;
import org.braekpo1nt.mctmanager.participant.Team;
import org.braekpo1nt.mctmanager.ui.sidebar.KeyLine;
import org.braekpo1nt.mctmanager.ui.sidebar.Sidebar;
import org.braekpo1nt.mctmanager.ui.timer.TimerManager;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * @param <P> the ParticipantData implementation used by this game
 * @param <T> the ScoredTeamData implementation used by this game
 * @param <QP> participant quit data type
 * @param <QT> team quit data type
 */
@Getter
@Setter
public abstract class GameBase<P extends ParticipantData, T extends ScoredTeamData<P>, QP, QT>  implements MCTGame, Listener {
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
    
    protected @NotNull Component title;
    
    public GameBase(
            @NotNull GameType type, 
            @NotNull Main plugin,
            @NotNull GameManager gameManager,
            @NotNull Component title,
            @NotNull Collection<Team> newTeams,
            @NotNull Collection<Participant> newParticipants,
            @NotNull List<Player> newAdmins) {
        this.type = type;
        this.plugin = plugin;
        this.gameManager = gameManager;
        this.sidebar = gameManager.createSidebar();
        this.adminSidebar = gameManager.createSidebar();
        this.participants = new HashMap<>(newParticipants.size());
        this.quitDatas = new HashMap<>();
        this.teams = new HashMap<>(newTeams.size());
        this.teamQuitDatas = new HashMap<>();
        this.title = title;
        this.timerManager = gameManager.getTimerManager().register(new TimerManager(plugin));
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
        this.admins = new ArrayList<>(newAdmins.size());
        for (Player admin : newAdmins) {
            _initializeAdmin(admin);
        }
        initializeAdminSidebar();
        // admin end
    }
    
    // Participant start
    private void _initializeParticipant(Participant newParticipant) {
        P participant = createParticipant(newParticipant);
        participants.put(participant.getUniqueId(), participant);
        sidebar.addPlayer(participant);
        T team = teams.get(participant.getTeamId());
        team.addParticipant(participant);
        participant.setGameMode(GameMode.ADVENTURE);
        ParticipantInitializer.clearStatusEffects(participant);
        ParticipantInitializer.clearInventory(participant);
        ParticipantInitializer.resetHealthAndHunger(participant);
        initializeParticipant(participant, team);
    }
    
    /**
     * @param participant the participant from which to derive the {@link P} type participant
     * @return the created {@link P}
     */
    protected abstract P createParticipant(Participant participant);
    
    /**
     * <p>Prepare the participant for the game</p>
     * <p>This is for any game-specific preparations</p>
     * @param participant the participant
     * @param team their team
     */
    protected abstract void initializeParticipant(P participant, T team);
    
    /**
     * @param newTeam the team from which to derive the {@link T} type team
     * @return the created {@link T} team 
     */
    protected abstract T createTeam(Team newTeam);
    
    /**
     * @param title the new title to display on the sidebar
     */
    @Override
    public void setTitle(@NotNull Component title) {
        this.title = title;
        sidebar.updateLine("title", title);
        adminSidebar.updateLine("title", title);
    }
    // Participant end
    
    // admin start
    private void _initializeAdmin(Player admin) {
        admins.add(admin);
        adminSidebar.addPlayer(admin);
        admin.setGameMode(GameMode.SPECTATOR);
        initializeAdmin(admin);
    }
    
    /**
     * <p>Prepare the admin for the game</p>
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
    
    public void displayScore(P participant) {
        sidebar.updateLine(participant.getUniqueId(), "personalScore", Component.empty()
                .append(Component.text("Personal: "))
                .append(Component.text(participant.getScore()))
                .color(NamedTextColor.GOLD));
    }
    
    // Sidebar end
    
}
