package org.braekpo1nt.mctmanager.games.experimental;

import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.braekpo1nt.mctmanager.games.game.enums.GameType;
import org.braekpo1nt.mctmanager.games.game.interfaces.MCTGame;
import org.braekpo1nt.mctmanager.participant.ParticipantData;
import org.braekpo1nt.mctmanager.participant.ParticipantID;
import org.braekpo1nt.mctmanager.participant.QuitDataBase;
import org.braekpo1nt.mctmanager.participant.ScoredTeamData;
import org.braekpo1nt.mctmanager.ui.UIManager;
import org.braekpo1nt.mctmanager.ui.sidebar.Sidebar;
import org.braekpo1nt.mctmanager.ui.tablist.TabList;
import org.braekpo1nt.mctmanager.ui.timer.TimerManager;
import org.bukkit.GameRule;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public abstract class DuoGameBase<P extends ParticipantData, T extends ScoredTeamData<P>, QP extends QuitDataBase, QT extends QuitDataBase, S extends GameStateBase<P, T>>  implements MCTGame, Listener, GameData<P> {
    protected final @NotNull GameType type;
    protected final @NotNull Main plugin;
    protected final @NotNull GameManager gameManager;
    protected final @NotNull TimerManager timerManager;
    protected final @NotNull Sidebar sidebar;
    protected final @NotNull Sidebar adminSidebar;
    protected final @NotNull TabList tabList;
    protected final @NotNull Map<UUID, P> participants;
    protected final @NotNull Map<ParticipantID, QP> quitDatas;
    protected final @NotNull T northTeam;
    protected final @NotNull T southTeam;
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
    
    public DuoGameBase(
            @NotNull GameType type,
            @NotNull Main plugin,
            @NotNull GameManager gameManager,
            @NotNull Component title,
            @NotNull S initialState,
            @NotNull T northTeam,
            @NotNull T southTeam) {
        this.type = type;
        this.plugin = plugin;
        this.gameManager = gameManager;
        this.sidebar = gameManager.createSidebar();
        this.adminSidebar = gameManager.createSidebar();
        this.tabList = new TabList(plugin);
        this.participants = new HashMap<>();
        this.northTeam = northTeam;
        this.southTeam = southTeam;
        this.quitDatas = new HashMap<>();
        this.title = title;
        this.uiManagers = new ArrayList<>();
        this.timerManager = gameManager.getTimerManager().register(new TimerManager(plugin));
        this.admins = new ArrayList<>();
        this.listeners = new ArrayList<>();
        this.storedGameRules = new ArrayList<>();
        this.state = initialState;
    }
}
