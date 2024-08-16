package org.braekpo1nt.mctmanager.games.game.capturetheflag;

import lombok.Data;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.config.exceptions.ConfigException;
import org.braekpo1nt.mctmanager.config.exceptions.ConfigIOException;
import org.braekpo1nt.mctmanager.config.exceptions.ConfigInvalidException;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.braekpo1nt.mctmanager.games.game.capturetheflag.config.CaptureTheFlagConfig;
import org.braekpo1nt.mctmanager.games.game.capturetheflag.config.CaptureTheFlagConfigController;
import org.braekpo1nt.mctmanager.games.game.capturetheflag.states.CaptureTheFlagState;
import org.braekpo1nt.mctmanager.games.game.capturetheflag.states.DescriptionState;
import org.braekpo1nt.mctmanager.games.game.enums.GameType;
import org.braekpo1nt.mctmanager.games.game.interfaces.Configurable;
import org.braekpo1nt.mctmanager.games.game.interfaces.MCTGame;
import org.braekpo1nt.mctmanager.ui.sidebar.Headerable;
import org.braekpo1nt.mctmanager.ui.sidebar.KeyLine;
import org.braekpo1nt.mctmanager.ui.sidebar.Sidebar;
import org.braekpo1nt.mctmanager.ui.timer.TimerManager;
import org.braekpo1nt.mctmanager.ui.topbar.BattleTopbar;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

@Data
public class CaptureTheFlagGame implements MCTGame, Configurable, Listener, Headerable {
    
    public @Nullable CaptureTheFlagState state;
    
    private final Main plugin;
    private final GameManager gameManager;
    private final BattleTopbar topbar;
    private final Component baseTitle = Component.empty()
            .append(Component.text("Capture the Flag"))
            .color(NamedTextColor.BLUE);
    private final TimerManager timerManager;
    private Component title = baseTitle;
    private RoundManager roundManager;
    private Sidebar sidebar;
    private Sidebar adminSidebar;
    private CaptureTheFlagConfigController configController;
    private CaptureTheFlagConfig config;
    private List<Player> participants = new ArrayList<>();
    private List<Player> admins = new ArrayList<>();
    private Map<UUID, Integer> killCount = new HashMap<>();
    private Map<UUID, Integer> deathCount = new HashMap<>();
    
    public CaptureTheFlagGame(Main plugin, GameManager gameManager) {
        this.plugin = plugin;
        this.gameManager = gameManager;
        this.timerManager = new TimerManager(plugin);
        this.configController = new CaptureTheFlagConfigController(plugin.getDataFolder());
        this.topbar = new BattleTopbar();
        
    }
    
    @Override
    public GameType getType() {
        return GameType.CAPTURE_THE_FLAG;
    }
    
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
    
    @Override
    public void loadConfig() throws ConfigIOException, ConfigInvalidException {
        if (state != null) {
            throw new ConfigException("CaptureTheFlagGame does not support loading the config mid-game");
        }
        this.config = configController.getConfig();
    }
    
    @Override
    public void start(List<Player> newParticipants, List<Player> newAdmins) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        participants = new ArrayList<>(newParticipants.size());
        sidebar = gameManager.getSidebarFactory().createSidebar();
        adminSidebar = gameManager.getSidebarFactory().createSidebar();
        roundManager = new RoundManager(config.getArenas().size());
        killCount = new HashMap<>(newParticipants.size());
        deathCount = new HashMap<>(newParticipants.size());
        for (Player participant : newParticipants) {
            initializeParticipant(participant);
        }
        initializeSidebar();
        startAdmins(newAdmins);
        setState(new DescriptionState(this));
        Main.logger().info("Starting Capture the Flag");
    }
    
    public void startNextRound(List<String> participantTeams, List<MatchPairing> roundMatchPairings) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
    
    private void initializeParticipant(Player participant) {
        participants.add(participant);
        sidebar.addPlayer(participant);
        topbar.showPlayer(participant);
        killCount.putIfAbsent(participant.getUniqueId(), 0);
        deathCount.putIfAbsent(participant.getUniqueId(), 0);
        int kills = killCount.get(participant.getUniqueId());
        int deaths = deathCount.get(participant.getUniqueId());
        topbar.setKillsAndDeaths(participant.getUniqueId(), kills, deaths);
    }
    
    private void startAdmins(List<Player> newAdmins) {
        this.admins = new ArrayList<>(newAdmins.size());
        for (Player admin : newAdmins) {
            initializeAdmin(admin);
        }
        initializeAdminSidebar();
    }
    
    private void initializeAdmin(Player admin) {
        admins.add(admin);
        adminSidebar.addPlayer(admin);
        admin.setGameMode(GameMode.SPECTATOR);
        admin.teleport(config.getSpawnObservatory());
    }
    
    private void resetAdmin(Player admin) {
        adminSidebar.removePlayer(admin);
    }
    
    @Override
    public void stop() {
        
    }
    
    @Override
    public void onParticipantJoin(Player participant) {
        
    }
    
    @Override
    public void onParticipantQuit(Player participant) {
        
    }
    
    @Override
    public void onAdminJoin(Player admin) {
        initializeAdmin(admin);
        adminSidebar.updateLine(admin.getUniqueId(), "title", title);
        String roundLine = String.format("Round %d/%d", roundManager.getPlayedRounds() + 1, roundManager.getMaxRounds());
        adminSidebar.updateLine("round", roundLine);
    }
    
    @Override
    public void onAdminQuit(Player admin) {
        resetAdmin(admin);
        admins.remove(admin);
    }
    
    private void initializeSidebar() {
        sidebar.addLines(
                new KeyLine("personalTeam", ""),
                new KeyLine("personalScore", ""),
                new KeyLine("title", title),
                new KeyLine("round", "")
        );
    }
    
    private void clearSidebar() {
        sidebar.deleteAllLines();
        sidebar = null;
        topbar.removeAllTeamPairs();
        topbar.hideAllPlayers();
    }
    
    private void initializeAdminSidebar() {
        adminSidebar.addLines(
                new KeyLine("title", title),
                new KeyLine("round", ""),
                new KeyLine("timer", "")
        );
    }
    
    private void clearAdminSidebar() {
        plugin.getLogger().info("delete all lines");
        adminSidebar.deleteAllLines();
        adminSidebar = null;
    }
    
    @Override
    public void updateTeamScore(Player participant, String contents) {
        if (sidebar == null) {
            return;
        }
        if (!participants.contains(participant)) {
            return;
        }
        sidebar.updateLine(participant.getUniqueId(), "personalTeam", contents);
    }
    
    @Override
    public void updatePersonalScore(Player participant, String contents) {
        if (sidebar == null) {
            return;
        }
        if (!participants.contains(participant)) {
            return;
        }
        sidebar.updateLine(participant.getUniqueId(), "personalScore", contents);
    }
    
    /**
     * Messages all the participants of the game (whether they're in a match or not)
     * @param message The message to send
     */
    public void messageAllParticipants(Component message) {
        gameManager.messageAdmins(message);
        for (Player participant : participants) {
            participant.sendMessage(message);
        }
    }
}
