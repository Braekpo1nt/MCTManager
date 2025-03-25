package org.braekpo1nt.mctmanager.games.experimental;

import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.braekpo1nt.mctmanager.games.game.enums.GameType;
import org.braekpo1nt.mctmanager.games.game.interfaces.MCTGame;
import org.braekpo1nt.mctmanager.participant.Participant;
import org.braekpo1nt.mctmanager.participant.ParticipantData;
import org.braekpo1nt.mctmanager.participant.ScoredTeamData;
import org.braekpo1nt.mctmanager.participant.Team;
import org.braekpo1nt.mctmanager.ui.sidebar.Sidebar;
import org.braekpo1nt.mctmanager.ui.timer.TimerManager;
import org.bukkit.entity.Player;
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
public abstract class GameBase<P extends ParticipantData, T extends ScoredTeamData<P>, QP, QT>  implements MCTGame {
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
        this.timerManager = new TimerManager(plugin);
        this.sidebar = gameManager.createSidebar();
        this.adminSidebar = gameManager.createSidebar();
        this.participants = new HashMap<>(newParticipants.size());
        this.quitDatas = new HashMap<>();
        this.teams = new HashMap<>(newTeams.size());
        this.teamQuitDatas = new HashMap<>();
        this.admins = new ArrayList<>(newAdmins.size());
        this.title = title;
    }
    
    /**
     * @param title the new title to display on the sidebar
     */
    @Override
    public void setTitle(@NotNull Component title) {
        this.title = title;
        sidebar.updateLine("title", title);
        adminSidebar.updateLine("title", title);
    }
    
}
