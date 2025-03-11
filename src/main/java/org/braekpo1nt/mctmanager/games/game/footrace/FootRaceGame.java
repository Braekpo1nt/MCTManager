package org.braekpo1nt.mctmanager.games.game.footrace;

import lombok.Data;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.config.exceptions.ConfigIOException;
import org.braekpo1nt.mctmanager.config.exceptions.ConfigInvalidException;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.braekpo1nt.mctmanager.games.game.enums.GameType;
import org.braekpo1nt.mctmanager.games.game.footrace.config.FootRaceConfig;
import org.braekpo1nt.mctmanager.games.game.footrace.config.FootRaceConfigController;
import org.braekpo1nt.mctmanager.games.game.footrace.states.DescriptionState;
import org.braekpo1nt.mctmanager.games.game.footrace.states.FootRaceState;
import org.braekpo1nt.mctmanager.games.game.interfaces.Configurable;
import org.braekpo1nt.mctmanager.games.game.interfaces.MCTGame;
import org.braekpo1nt.mctmanager.games.utils.GameManagerUtils;
import org.braekpo1nt.mctmanager.games.utils.ParticipantInitializer;
import org.braekpo1nt.mctmanager.participant.Participant;
import org.braekpo1nt.mctmanager.participant.Team;
import org.braekpo1nt.mctmanager.ui.sidebar.KeyLine;
import org.braekpo1nt.mctmanager.ui.sidebar.Sidebar;
import org.braekpo1nt.mctmanager.ui.timer.TimerManager;
import org.braekpo1nt.mctmanager.utils.BlockPlacementUtils;
import org.braekpo1nt.mctmanager.utils.LogType;
import org.braekpo1nt.mctmanager.utils.MathUtils;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

@Data
public class FootRaceGame implements Listener, MCTGame, Configurable {
    
    private @Nullable FootRaceState state;
    
    public static final long COOL_DOWN_TIME = 3000L;
    private final FootRaceConfigController configController;
    private final Main plugin;
    private final GameManager gameManager;
    private final PotionEffect SPEED = new PotionEffect(PotionEffectType.SPEED, 10000, 8, true, false, false);
    private final PotionEffect INVISIBILITY = new PotionEffect(PotionEffectType.INVISIBILITY, 10000, 1, true, false, false);
    private final Component baseTitle = Component.empty()
            .append(Component.text("Foot Race"))
            .color(NamedTextColor.BLUE);
    private final TimerManager timerManager;
    private FootRaceConfig config;
    private Sidebar sidebar;
    private Sidebar adminSidebar;
    private int timerRefreshTaskId;
    private List<Player> admins = new ArrayList<>();
    
    private Map<UUID, FootRaceParticipant> participants = new HashMap<>();
    /**
     * Holds the data for participants when they have quit mid-round
     */
    private Map<UUID, FootRaceParticipant.QuitData> quitDatas = new HashMap<>();
    private Map<String, FootRaceTeam> teams = new HashMap<>();
    private Map<String, FootRaceTeam.QuitData> teamQuitDatas = new HashMap<>();
    /**
     * what place every participant is in at any given moment in the race
     */
    private List<FootRaceParticipant> standings;
    /**
     * Keeps track of how many participants have finished the race, so we can know 
     * what place a player should be in when they finish
     * TODO: Participant would {@link #standings} be sufficient for this?
     */
    private int numOfFinishedParticipants = 0;
    private long raceStartTime;
    private int statusEffectsTaskId;
    private int standingsDisplayTaskId;
    private Component title = baseTitle;
    
    public FootRaceGame(Main plugin, GameManager gameManager) {
        this.plugin = plugin;
        this.gameManager = gameManager;
        this.timerManager = new TimerManager(plugin);
        this.configController = new FootRaceConfigController(plugin.getDataFolder());
    }
    
    @Override
    public void loadConfig() throws ConfigIOException, ConfigInvalidException {
        this.config = configController.getConfig();
    }
    
    @Override
    public GameType getType() {
        return GameType.FOOT_RACE;
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
    public void start(Collection<Team> newTeams, Collection<Participant> newParticipants, List<Player> newAdmins) {
        this.participants = new HashMap<>(newParticipants.size());
        this.teams = new HashMap<>(newTeams.size());
        for (Team newTeam : newTeams) {
            FootRaceTeam team = new FootRaceTeam(newTeam, 0);
            this.teams.put(team.getTeamId(), team);
        }
        this.quitDatas = new HashMap<>();
        this.teamQuitDatas = new HashMap<>();
        standings = new ArrayList<>(newParticipants.size());
        admins = new ArrayList<>(newAdmins.size());
        sidebar = gameManager.createSidebar();
        adminSidebar = gameManager.createSidebar();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        gameManager.getTimerManager().register(timerManager);
        closeGlassBarrier();
        for (Participant participant : newParticipants) {
            initializeParticipant(participant);
        }
        startAdmins(newAdmins);
        initializeSidebar();
        updateStandings();
        displayStandings();
        startStatusEffectsTask();
        setupTeamOptions();
        state = new DescriptionState(this);
        Main.logger().info("Starting Foot Race game");
    }
    
    public void updateStandings() {
        standings = standings.stream()
                .sorted((participant1, participant2) -> {
                    if (participant1.isFinished() || participant2.isFinished()) {
                        if (participant1.isFinished() && participant2.isFinished()) {
                            return participant1.getPlacement() - participant2.getPlacement();
                        } else {
                            if (participant1.isFinished()) {
                                return -1;
                            } else {
                                return 1;
                            }
                        }
                    }
                    
                    if (participant1.getLap() != participant2.getLap()) {
                        return participant2.getLap() - participant1.getLap(); // Reverse order
                    }
                    
                    int nextCheckpoint1 = MathUtils.wrapIndex(participant1.getCurrentCheckpoint() + 1, config.getCheckpoints().size());
                    int nextCheckpoint2 = MathUtils.wrapIndex(participant2.getCurrentCheckpoint() + 1, config.getCheckpoints().size());
                    if (nextCheckpoint1 != nextCheckpoint2) {
                        return nextCheckpoint2 - nextCheckpoint1; // Reverse order
                    }
                    
                    BoundingBox checkpoint = config.getCheckpoints().get(nextCheckpoint1);
                    double distance1 = MathUtils.getMinimumDistance(checkpoint, participant1.getLocation().toVector());
                    double distance2 = MathUtils.getMinimumDistance(checkpoint, participant2.getLocation().toVector());
                    if (distance1 != distance2) {
                        return Double.compare(distance1, distance2);
                    }
                    
                    return participant1.getName().compareTo(participant2.getName());
                }).collect(Collectors.toCollection(ArrayList::new));
    }
    
    public void displayStandings() {
        for (int i = 0; i < standings.size(); i++) {
            FootRaceParticipant participant = standings.get(i);
            List<KeyLine> standingLines = createStandingLines(i);
            sidebar.updateLines(participant.getUniqueId(), standingLines);
        }
        adminSidebar.updateLines(createStandingLines(0));
    }
    
    private List<KeyLine> createStandingLines(int standing) {
        // there are 5 or fewer participants, or the standing is top 4
        if (standings.size() <= 5 || (0 <= standing && standing <= 3)) {
            return List.of(
                    new KeyLine("standing1", standingLine(0)),
                    new KeyLine("standing2", standingLine(1)),
                    new KeyLine("standing3", standingLine(2)),
                    new KeyLine("standing4", standingLine(3)),
                    new KeyLine("standing5", standingLine(4))
            );
        }
        // last place
        if (standing == standings.size() - 1) {
            return List.of(
                    new KeyLine("standing1", standingLine(0)),
                    new KeyLine("standing2", Component.text("...").color(NamedTextColor.GRAY)),
                    new KeyLine("standing3", standingLine(standing - 2)),
                    new KeyLine("standing4", standingLine(standing - 1)),
                    new KeyLine("standing5", standingLine(standing))
            );
        }
        // 5th place or lower (but not last)
        return List.of(
                new KeyLine("standing1", standingLine(0)),
                new KeyLine("standing2", Component.text("...").color(NamedTextColor.GRAY)),
                new KeyLine("standing3", standingLine(standing - 1)),
                new KeyLine("standing4", standingLine(standing)),
                new KeyLine("standing5", standingLine(standing + 1))
        );
    }
    
    private Component standingLine(int standing) {
        if (standing < 0 || standings.size() <= standing) {
            return Component.empty();
        }
        return Component.empty()
                .append(Component.text(standing + 1))
                .append(Component.text(". "))
                .append(standings.get(standing).displayName())
                ;
    }
    
    public void closeGlassBarrier() {
        BlockPlacementUtils.createCubeReplace(config.getWorld(), config.getGlassBarrier(), Material.AIR, Material.WHITE_STAINED_GLASS_PANE);
        BlockPlacementUtils.updateDirection(config.getWorld(), config.getGlassBarrier());
    }
    
    public void openGlassBarrier() {
        BlockPlacementUtils.createCubeReplace(config.getWorld(), config.getGlassBarrier(), Material.WHITE_STAINED_GLASS_PANE, Material.AIR);
    }
    
    private void startStatusEffectsTask() {
        this.statusEffectsTaskId = new BukkitRunnable(){
            @Override
            public void run() {
                for (Participant participant : participants.values()) {
                    participant.addPotionEffect(SPEED);
                    participant.addPotionEffect(INVISIBILITY);
                }
            }
        }.runTaskTimer(plugin, 0L, 60L).getTaskId();
    }
    
    private void setupTeamOptions() {
        Scoreboard mctScoreboard = gameManager.getMctScoreboard();
        for (org.bukkit.scoreboard.Team team : mctScoreboard.getTeams()) {
            team.setAllowFriendlyFire(false);
            team.setCanSeeFriendlyInvisibles(true);
            team.setOption(org.bukkit.scoreboard.Team.Option.NAME_TAG_VISIBILITY, org.bukkit.scoreboard.Team.OptionStatus.ALWAYS);
            team.setOption(org.bukkit.scoreboard.Team.Option.DEATH_MESSAGE_VISIBILITY, org.bukkit.scoreboard.Team.OptionStatus.ALWAYS);
            team.setOption(org.bukkit.scoreboard.Team.Option.COLLISION_RULE, org.bukkit.scoreboard.Team.OptionStatus.NEVER);
        }
    }
    
    public void initializeParticipant(Participant newParticipant) {
        FootRaceParticipant participant = new FootRaceParticipant(newParticipant, config.getCheckpoints().size() - 1, 0);
        participants.put(participant.getUniqueId(), participant);
        teams.get(participant.getTeamId()).addParticipant(participant);
        standings.add(participant);
        sidebar.addPlayer(participant);
        participant.teleport(config.getStartingLocation());
        participant.setRespawnLocation(config.getStartingLocation(), true);
        ParticipantInitializer.clearInventory(participant);
        giveBoots(participant);
        participant.setGameMode(GameMode.ADVENTURE);
        ParticipantInitializer.clearStatusEffects(participant);
        ParticipantInitializer.resetHealthAndHunger(participant);
    }
    
    public void giveBoots(Participant participant) {
        Color teamColor = gameManager.getTeam(participant).getBukkitColor();
        ItemStack boots = new ItemStack(Material.LEATHER_BOOTS);
        LeatherArmorMeta meta = (LeatherArmorMeta) boots.getItemMeta();
        meta.setColor(teamColor);
        boots.setItemMeta(meta);
        participant.getEquipment().setBoots(boots);
    }
    
    @Override
    public void stop() {
        HandlerList.unregisterAll(this);
        closeGlassBarrier();
        cancelAllTasks();
        stopAdmins();
        saveScores();
        for (FootRaceParticipant participant : participants.values()) {
            resetParticipant(participant);
        }
        clearSidebar();
        participants.clear();
        teams.clear();
        quitDatas.clear();
        teamQuitDatas.clear();
        standings.clear();
        gameManager.gameIsOver();
        Main.logger().info("Stopping Foot Race game");
    }
    
    private void saveScores() {
        Map<String, Integer> teamScores = new HashMap<>();
        Map<UUID, Integer> participantScores = new HashMap<>();
        for (FootRaceTeam team : teams.values()) {
            teamScores.put(team.getTeamId(), team.getScore());
        }
        for (FootRaceParticipant participant : participants.values()) {
            participantScores.put(participant.getUniqueId(), participant.getScore());
        }
        for (Map.Entry<String, FootRaceTeam.QuitData> entry : teamQuitDatas.entrySet()) {
            teamScores.put(entry.getKey(), entry.getValue().getScore());
        }
        for (Map.Entry<UUID, FootRaceParticipant.QuitData> entry : quitDatas.entrySet()) {
            participantScores.put(entry.getKey(), entry.getValue().getScore());
        }
        gameManager.addScores(teamScores, participantScores);
    }
    
    private void cancelAllTasks() {
        Bukkit.getScheduler().cancelTask(timerRefreshTaskId);
        Bukkit.getScheduler().cancelTask(statusEffectsTaskId);
        Bukkit.getScheduler().cancelTask(standingsDisplayTaskId);
        timerManager.cancel();
    }
    
    public void resetParticipant(FootRaceParticipant participant) {
        teams.get(participant.getTeamId()).removeParticipant(participant.getUniqueId());
        ParticipantInitializer.clearInventory(participant);
        ParticipantInitializer.clearStatusEffects(participant);
        ParticipantInitializer.resetHealthAndHunger(participant);
        sidebar.removePlayer(participant);
    }
    
    public void onTeamJoin(Team team) {
        if (teams.containsKey(team.getTeamId())) {
            return;
        }
        FootRaceTeam.QuitData quitData = teamQuitDatas.get(team.getTeamId());
        if (quitData != null) {
            teams.put(team.getTeamId(), new FootRaceTeam(team, quitData.getScore()));
        } else {
            teams.put(team.getTeamId(), new FootRaceTeam(team, 0));
        }
    }
    
    @Override
    public void onParticipantJoin(Participant participant, Team team) {
        if (state != null) {
            state.onParticipantJoin(participant, team);
        }
    }
    
    public void onTeamQuit(FootRaceTeam team) {
        if (team.size() > 0) {
            return;
        }
        FootRaceTeam removed = teams.remove(team.getTeamId());
        teamQuitDatas.put(team.getTeamId(), removed.getQuitData());
    }
    
    @Override
    public void onParticipantQuit(Participant participant, Team team) {
        FootRaceParticipant footRaceParticipant = participants.get(participant.getUniqueId());
        if (footRaceParticipant == null) {
            return;
        }
        FootRaceTeam footRaceTeam = teams.get(participant.getTeamId());
        if (state != null) {
            state.onParticipantQuit(footRaceParticipant, footRaceTeam);
        }
    }
    
    private void startAdmins(List<Player> newAdmins) {
        this.admins = new ArrayList<>(newAdmins.size());
        for (Player admin : newAdmins) {
            initializeAdmin(admin);
        }
        initializeAdminSidebar();
    }
    
    private void initializeAdminSidebar() {
        adminSidebar.addLines(
                new KeyLine("title", title),
                new KeyLine("elapsedTime", "00:00:000"),
                new KeyLine("timer", Component.empty()),
                new KeyLine("standing1", Component.empty()),
                new KeyLine("standing2", Component.empty()),
                new KeyLine("standing3", Component.empty()),
                new KeyLine("standing4", Component.empty()),
                new KeyLine("standing5", Component.empty())
        );
    }
    
    @Override
    public void onAdminJoin(Player admin) {
        initializeAdmin(admin);
        adminSidebar.updateLine(admin.getUniqueId(), "title", title);
    }
    
    private void initializeAdmin(Player admin) {
        admins.add(admin);
        adminSidebar.addPlayer(admin);
        admin.setGameMode(GameMode.SPECTATOR);
        admin.teleport(config.getStartingLocation());
    }
    
    @Override
    public void onAdminQuit(Player admin) {
        resetAdmin(admin);
        admins.remove(admin);
    }
    
    private void stopAdmins() {
        for (Player admin : admins) {
            resetAdmin(admin);
        }
        clearAdminSidebar();
        admins.clear();
    }
    
    private void resetAdmin(Player admin) {
        adminSidebar.removePlayer(admin);
    }
    
    private void clearAdminSidebar() {
        adminSidebar.deleteAllLines();
        adminSidebar = null;
    }
    
    private void initializeSidebar() {
        sidebar.addLines(
                new KeyLine("personalTeam", ""),
                new KeyLine("personalScore", ""),
                new KeyLine("title", title),
                new KeyLine("elapsedTime", "00:00:000"),
                new KeyLine("lap", Component.empty()
                        .append(Component.text("Lap: 1/"))
                        .append(Component.text(config.getLaps()))),
                new KeyLine("timer", Component.empty()),
                new KeyLine("standing1", Component.empty()),
                new KeyLine("standing2", Component.empty()),
                new KeyLine("standing3", Component.empty()),
                new KeyLine("standing4", Component.empty()),
                new KeyLine("standing5", Component.empty())
        );
        for (FootRaceTeam team : teams.values()) {
            displayScore(team);
        }
        for (FootRaceParticipant participant : participants.values()) {
            displayScore(participant);
        }
    }
    
    public void displayScore(FootRaceTeam team) {
        Component contents = Component.empty()
                .append(team.getFormattedDisplayName())
                .append(Component.text(": "))
                .append(Component.text(team.getScore())
                        .color(NamedTextColor.GOLD));
        for (UUID memberUUID : team.getMemberUUIDs()) {
            sidebar.updateLine(memberUUID, "personalTeam", contents);
        }
    }
    
    public void displayScore(FootRaceParticipant participant) {
        sidebar.updateLine(participant.getUniqueId(), "personalScore", Component.empty()
                .append(Component.text("Personal: "))
                .append(Component.text(participant.getScore()))
                .color(NamedTextColor.GOLD));
    }
    
    private void clearSidebar() {
        sidebar.deleteAllLines();
        sidebar = null;
    }
    
    // EventHandlers
    
    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        if (config.getSpectatorArea() == null){
            return;
        }
        if (!participants.containsKey(event.getPlayer().getUniqueId())) {
            return;
        }
        if (!event.getPlayer().getGameMode().equals(GameMode.SPECTATOR)) {
            return;
        }
        if (!event.getCause().equals(PlayerTeleportEvent.TeleportCause.SPECTATE)) {
            return;
        }
        if (!config.getSpectatorArea().contains(event.getTo().toVector())) {
            event.setCancelled(true);
        }
    }
    
    /**
     * Stop players from removing their equipment
     */
    @EventHandler
    public void onClickInventory(InventoryClickEvent event) {
        if (event.getClickedInventory() == null) {
            return;
        }
        if (event.getCurrentItem() == null) {
            return;
        }
        if (!participants.containsKey(event.getWhoClicked().getUniqueId())) {
            return;
        }
        event.setCancelled(true);
    }
    
    /**
     * Stop players from dropping items
     */
    @EventHandler
    public void onDropItem(PlayerDropItemEvent event) {
        if (!participants.containsKey(event.getPlayer().getUniqueId())) {
            return;
        }
        event.setCancelled(true);
    }
    
    @EventHandler
    public void onPlayerDamage(EntityDamageEvent event) {
        if (GameManagerUtils.EXCLUDED_CAUSES.contains(event.getCause())) {
            return;
        }
        if (!participants.containsKey(event.getEntity().getUniqueId())) {
            return;
        }
        Main.debugLog(LogType.CANCEL_ENTITY_DAMAGE_EVENT, "FootraceGame.onPlayerDamage() cancelled");
        event.setCancelled(true);
    }
    
    @EventHandler
    public void onPlayerLoseHunger(FoodLevelChangeEvent event) {
        Participant participant = participants.get(event.getEntity().getUniqueId());
        if (participant == null) {
            return;
        }
        participant.setFoodLevel(20);
        event.setCancelled(true);
    }
    
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Block clickedBlock = event.getClickedBlock();
        if (clickedBlock == null) {
            return;
        }
        if (!participants.containsKey(event.getPlayer().getUniqueId())) {
            return;
        }
        Material blockType = clickedBlock.getType();
        if (!config.getPreventInteractions().contains(blockType)) {
            return;
        }
        event.setCancelled(true);
    }
    
    // state calling methods
    
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        FootRaceParticipant participant = participants.get(event.getPlayer().getUniqueId());
        if (participant == null) {
            return;
        }
        if (state != null) {
            state.onParticipantMove(participant);
        }
        if (participant.getGameMode().equals(GameMode.SPECTATOR)) {
            keepSpectatorsInArea(participant, event);
        }
    }
    
    /**
     * Prevent spectators from leaving the spectatorArea
     * @param participant the participant (assumed to be a valid participant of this game in the SPECTATOR gamemode
     * @param event the event which may be cancelled in order to keep the given participant in the spectator area
     */
    private void keepSpectatorsInArea(@NotNull Participant participant, PlayerMoveEvent event) {
        if (config.getSpectatorArea() == null){
            return;
        }
        if (!config.getSpectatorArea().contains(event.getFrom().toVector())) {
            participant.teleport(config.getStartingLocation());
            return;
        }
        if (!config.getSpectatorArea().contains(event.getTo().toVector())) {
            event.setCancelled(true);
        }
    }
    
    public void messageAllParticipants(Component message) {
        gameManager.messageAdmins(message);
        Audience.audience(participants.values()).sendMessage(message);
    }
}
