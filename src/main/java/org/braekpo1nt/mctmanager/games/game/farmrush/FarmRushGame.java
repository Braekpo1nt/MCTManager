package org.braekpo1nt.mctmanager.games.game.farmrush;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.audience.MessageType;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.config.exceptions.ConfigIOException;
import org.braekpo1nt.mctmanager.config.exceptions.ConfigInvalidException;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.braekpo1nt.mctmanager.games.game.enums.GameType;
import org.braekpo1nt.mctmanager.games.game.farmrush.config.FarmRushConfig;
import org.braekpo1nt.mctmanager.games.game.farmrush.config.FarmRushConfigController;
import org.braekpo1nt.mctmanager.games.game.farmrush.states.DescriptionState;
import org.braekpo1nt.mctmanager.games.game.farmrush.states.FarmRushState;
import org.braekpo1nt.mctmanager.games.game.interfaces.Configurable;
import org.braekpo1nt.mctmanager.games.game.interfaces.MCTGame;
import org.braekpo1nt.mctmanager.games.utils.ParticipantInitializer;
import org.braekpo1nt.mctmanager.ui.sidebar.Headerable;
import org.braekpo1nt.mctmanager.ui.sidebar.KeyLine;
import org.braekpo1nt.mctmanager.ui.sidebar.Sidebar;
import org.braekpo1nt.mctmanager.ui.timer.TimerManager;
import org.braekpo1nt.mctmanager.utils.BlockPlacementUtils;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.*;
import java.util.List;

@Data
public class FarmRushGame implements MCTGame, Configurable, Headerable, Listener {
    
    private final Main plugin;
    private final GameManager gameManager;
    private final TimerManager timerManager;
    private final Component baseTitle = Component.empty()
            .append(Component.text("Farm Rush"))
            .color(NamedTextColor.BLUE);
    private Component title = baseTitle;
    private @Nullable FarmRushState state;
    private FarmRushConfig config;
    private final FarmRushConfigController configController;
    private final List<Player> admins = new ArrayList<>();
    private Sidebar sidebar;
    private Sidebar adminSidebar;
    private final Map<UUID, Participant> participants = new HashMap<>();
    private final Map<String, Team> teams = new HashMap<>();
    
    @Data
    @EqualsAndHashCode(onlyExplicitlyIncluded = true)
    public static class Participant {
        /**
         * The player object that this participant represents
         */
        @EqualsAndHashCode.Include
        private final @NotNull Player player;
        private final @NotNull String teamId;
    }
    
    @Data
    @EqualsAndHashCode(onlyExplicitlyIncluded = true)
    public static class Team {
        @EqualsAndHashCode.Include
        private final @NotNull String teamId;
        private final @NotNull List<UUID> members = new ArrayList<>();
        private final Arena arena;
    }
    
    public FarmRushGame(Main plugin, GameManager gameManager) {
        this.plugin = plugin;
        this.gameManager = gameManager;
        this.timerManager = new TimerManager(plugin);
        this.configController = new FarmRushConfigController(plugin.getDataFolder());
    }
    
    @Override
    public void loadConfig() throws ConfigIOException, ConfigInvalidException {
        this.config = configController.getConfig();
    }
    
    @Override
    public GameType getType() {
        return GameType.FARM_RUSH;
    }
    
    @Override
    public void start(List<Player> newParticipants, List<Player> newAdmins) {
        sidebar = gameManager.getSidebarFactory().createSidebar();
        adminSidebar = gameManager.getSidebarFactory().createSidebar();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        gameManager.getTimerManager().register(timerManager);
        List<String> teamIds = gameManager.getTeamIds(newParticipants);
        List<Arena> arenas = createArenas(teamIds);
        for (int i = 0; i < teamIds.size(); i++) {
            String teamId = teamIds.get(i);
            Arena arena = arenas.get(i);
            teams.put(teamId, new Team(teamId, arena));
        }
        placeArenas(arenas);
        for (Player participant : newParticipants) {
            initializeParticipant(participant);
        }
        startAdmins(newAdmins);
        initializeSidebar();
        setupTeamOptions();
        state = new DescriptionState(this);
        Main.logger().info("Starting Farm Rush game");
    }
    
    /**
     * Actually place the schematic file of the arenas and add any necessary additions,
     * such as the barrel for delivery
     * @param arenas the arenas to place copies of the schematic file on
     */
    private void placeArenas(@NotNull List<Arena> arenas) {
        File schematicFile = new File(plugin.getDataFolder(), config.getArenaFileName());
        List<Vector> origins = arenas.stream().map(arena -> arena.getBounds().getMin()).toList();
        BlockPlacementUtils.placeSchematic(config.getWorld(), origins, schematicFile);
        for (Arena arena : arenas) {
            Block block = arena.getDelivery().getBlock();
            block.setType(Material.BARREL);
            Directional directional = (Directional) block.getBlockData();
            directional.setFacing(arena.getDeliveryBlockFace());
            block.setBlockData(directional);
        }
    }
    
    /**
     * Fill the space that the arenas were placed with air
     * @param arenas the arenas to remove
     */
    private void removeArenas(@NotNull List<Arena> arenas) {
        List<BoundingBox> boxes = arenas.stream().map(Arena::getBounds).toList();
        BlockPlacementUtils.fillWithAir(config.getWorld(), boxes);
    }
    
    private @NotNull List<Arena> createArenas(List<String> teamIds) {
        List<Arena> arenas = new ArrayList<>(teamIds.size());
        Arena firstArena = config.getFirstArena();
        Vector offset = new Vector(firstArena.getBounds().getWidthX(), 0, 0);
        for (int i = 0; i < teamIds.size(); i++) {
            arenas.add(firstArena);
            firstArena = firstArena.offset(offset);
        }
        return arenas;
    }
    
    private void initializeParticipant(Player player) {
        String teamId = gameManager.getTeamId(player.getUniqueId());
        Participant participant = new Participant(player, teamId);
        participants.put(player.getUniqueId(), participant);
        Team team = teams.get(teamId);
        team.getMembers().add(player.getUniqueId());
        player.setGameMode(GameMode.ADVENTURE);
        player.teleport(team.getArena().getSpawn());
        player.setRespawnLocation(team.getArena().getSpawn());
        sidebar.addPlayer(player);
        ParticipantInitializer.clearInventory(player);
        ParticipantInitializer.clearStatusEffects(player);
        ParticipantInitializer.resetHealthAndHunger(player);
    }
    
    private void startAdmins(List<Player> newAdmins) {
        for (Player admin : newAdmins) {
            initializeAdmin(admin);
        }
        initializeAdminSidebar();
    }
    
    private void initializeAdmin(Player admin) {
        admins.add(admin);
        adminSidebar.addPlayer(admin);
        admin.setGameMode(GameMode.SPECTATOR);
        admin.teleport(config.getAdminLocation());
    }
    
    private void initializeAdminSidebar() {
        adminSidebar.addLines(
                new KeyLine("title", title)
        );
    }
    
    private void clearAdminSidebar() {
        adminSidebar.deleteAllLines();
        adminSidebar = null;
    }
    
    @Override
    public void stop() {
        participants.clear();
        stopAdmins();
        cancelAllTasks();
        removeArenas(teams.values().stream().map(Team::getArena).toList());
    }
    
    private void stopAdmins() {
        for (Player admin : admins) {
            resetAdmin(admin);
        }
        clearAdminSidebar();
        admins.clear();
    }
    
    private void cancelAllTasks() {
        timerManager.cancel();
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
    }
    
    @Override
    public void onAdminQuit(Player admin) {
        resetAdmin(admin);
        admins.remove(admin);
    }
    
    private void resetAdmin(Player admin) {
        adminSidebar.removePlayer(admin);
    }
    
    @Override
    public @NotNull Component getBaseTitle() {
        return baseTitle;
    }
    
    @Override
    public void setTitle(@NotNull Component title) {
        this.title = title;
    }
    
    @Override
    public void updatePersonalScore(Player participant, Component contents) {
        if (sidebar == null) {
            return;
        }
        if (!participants.containsKey(participant.getUniqueId())) {
            return;
        }
        sidebar.updateLine(participant.getUniqueId(), "personalScore", contents);
    }
    
    @Override
    public void updateTeamScore(Player participant, Component contents) {
        if (sidebar == null) {
            return;
        }
        if (!participants.containsKey(participant.getUniqueId())) {
            return;
        }
        sidebar.updateLine(participant.getUniqueId(), "personalTeam", contents);
    }
    
    private void initializeSidebar() {
        sidebar.addLines(
                new KeyLine("personalTeam", ""),
                new KeyLine("personalScore", ""),
                new KeyLine("title", title)
        );
    }
    
    private void clearSidebar() {
        sidebar.deleteAllLines();
        sidebar = null;
    }
    
    private void setupTeamOptions() {
        Scoreboard mctScoreboard = gameManager.getMctScoreboard();
        for (org.bukkit.scoreboard.Team team : mctScoreboard.getTeams()) {
            team.setAllowFriendlyFire(false);
            team.setCanSeeFriendlyInvisibles(true);
            team.setOption(org.bukkit.scoreboard.Team.Option.NAME_TAG_VISIBILITY, org.bukkit.scoreboard.Team.OptionStatus.ALWAYS);
            team.setOption(org.bukkit.scoreboard.Team.Option.DEATH_MESSAGE_VISIBILITY, org.bukkit.scoreboard.Team.OptionStatus.ALWAYS);
            team.setOption(org.bukkit.scoreboard.Team.Option.COLLISION_RULE, org.bukkit.scoreboard.Team.OptionStatus.ALWAYS);
        }
    }
    
    public void messageAllParticipants(Component message) {
        Audience.audience(admins
        ).sendMessage(message);
        for (Participant participant : participants.values()) {
            participant.getPlayer().sendMessage(message);
        }
    }
}
