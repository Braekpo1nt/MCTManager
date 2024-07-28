package org.braekpo1nt.mctmanager.games.event;

import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.braekpo1nt.mctmanager.games.colossalcombat.ColossalCombatGame;
import org.braekpo1nt.mctmanager.games.event.config.EventConfig;
import org.braekpo1nt.mctmanager.games.event.config.EventConfigController;
import org.braekpo1nt.mctmanager.games.event.states.EventState;
import org.braekpo1nt.mctmanager.games.game.enums.GameType;
import org.braekpo1nt.mctmanager.games.voting.VoteManager;
import org.braekpo1nt.mctmanager.ui.sidebar.Sidebar;
import org.braekpo1nt.mctmanager.ui.timer.TimerManager;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EventManager implements Listener {
    private @Nullable EventState state;
    
    private final Main plugin;
    private final GameManager gameManager;
    private final VoteManager voteManager;
    private final ColossalCombatGame colossalCombatGame;
    private final EventConfigController configController;
    private final List<GameType> playedGames = new ArrayList<>();
    /**
     * contains the ScoreKeepers for the games played during the event. Cleared on start and end of event. 
     * <p>
     * If a given key doesn't exist, no score was kept for that game. 
     * <p>
     * If a given key does exist, it is pared with a list of ScoreKeepers which contain the scores
     * tracked for a given iteration of the game. Iterations are in order of play, first to last.
     * If a given iteration is null, then no points were tracked for that iteration. 
     * Otherwise, it contains the scores tracked for the given iteration. 
     */
    private final Map<GameType, List<ScoreKeeper>> scoreKeepers = new HashMap<>();
    private final ItemStack crown = new ItemStack(Material.CARVED_PUMPKIN);
    private final TimerManager timerManager;
    private Sidebar sidebar;
    private Sidebar adminSidebar;
    private int numberOfTeams = 0;
    private EventConfig config;
    private int maxGames = 6;
    private int currentGameNumber = 0;
    private List<Player> participants = new ArrayList<>();
    private List<Player> admins = new ArrayList<>();
    private String winningTeam;
    
    public EventManager(Main plugin, GameManager gameManager, VoteManager voteManager) {
        this.plugin = plugin;
        this.timerManager = new TimerManager(plugin);
        this.gameManager = gameManager;
        this.voteManager = voteManager;
        this.configController = new EventConfigController(plugin.getDataFolder());
        this.colossalCombatGame = new ColossalCombatGame(plugin, gameManager);
        this.crown.editMeta(meta -> meta.setCustomModelData(1));
    }
    
    public void onParticipantJoin(Player participant) {
        if (state != null) {
            state.onParticipantJoin(participant);
        }
    }
    
    public void onParticipantQuit(Player participant) {
        if (state != null) {
            state.onParticipantQuit(participant);
        }
    }
    
    public void onAdminJoin(Player admin) {
        if (state != null) {
            state.onAdminJoin(admin);
        }
    }
    
    public void onAdminQuit(Player admin) {
        if (state != null) {
            state.onAdminQuit(admin);
        }
    }
    
    public void startEvent(CommandSender sender, int numberOfGames) {
        if (state != null) {
            state.startEvent(sender, numberOfGames);
        }
    }
    
    public void stopEvent(CommandSender sender) {
        if (state != null) {
            stopEvent(sender);
        }
    }
    
    public void undoGame(@NotNull CommandSender sender, @NotNull GameType gameType, int iterationIndex) {
        if (state != null) {
            state.undoGame(sender, gameType, iterationIndex);
        }
    }
    
    @EventHandler
    public void onPlayerDamage(EntityDamageEvent event) {
        if (state != null) {
            state.onPlayerDamage(event);
        }
    }
    
    @EventHandler
    public void onClickInventory(InventoryClickEvent event) {
        if (state != null) {
            state.onClickInventory(event);
        }
    }
    
    @EventHandler
    public void onDropItem(PlayerDropItemEvent event) {
        if (state != null) {
            state.onDropItem(event);
        }
    }
}
