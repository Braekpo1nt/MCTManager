package org.braekpo1nt.mctmanager.games.experimental;

import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.braekpo1nt.mctmanager.games.game.enums.GameType;
import org.braekpo1nt.mctmanager.games.game.interfaces.MCTGame;
import org.braekpo1nt.mctmanager.participant.ParticipantData;
import org.braekpo1nt.mctmanager.participant.ScoredTeamData;
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
    protected final GameType type;
    // TODO: baseTitle can just be title
    protected final Component baseTitle;
    protected final Main plugin;
    protected final GameManager gameManager;
    protected final TimerManager timerManager;
    protected final Sidebar sidebar;
    protected final Sidebar adminSidebar;
    protected final Map<UUID, P> participants;
    protected final Map<UUID, QP> quitDatas;
    protected final Map<String, T> teams;
    protected final Map<String, QT> teamQuitDatas;
    protected final List<Player> admins;
    
    protected Component title;
    
    public GameBase(GameType type, Component baseTitle, Main plugin, GameManager gameManager) {
        this.type = type;
        this.baseTitle = baseTitle;
        this.plugin = plugin;
        this.gameManager = gameManager;
        this.timerManager = new TimerManager(plugin);
        this.sidebar = gameManager.createSidebar();
        this.adminSidebar = gameManager.createSidebar();
        this.participants = new HashMap<>();
        this.quitDatas = new HashMap<>();
        this.teams = new HashMap<>();
        this.teamQuitDatas = new HashMap<>();
        this.admins = new ArrayList<>();
        
        this.title = this.baseTitle;
    }
    
    /**
     * @param title
     * @deprecated 
     */
    @Deprecated
    @Override
    public void setTitle(@NotNull Component title) {
        this.title = title;
        if (sidebar != null) {
            sidebar.updateLine("title", title);
        }
        if (adminSidebar != null) {
            adminSidebar.updateLine("title", title);
        }
    }
    
    @Override
    public @NotNull Component getBaseTitle() {
        return baseTitle;
    }
    
}
