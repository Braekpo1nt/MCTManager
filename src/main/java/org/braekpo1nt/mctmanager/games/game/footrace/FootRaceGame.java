package org.braekpo1nt.mctmanager.games.game.footrace;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.config.exceptions.ConfigIOException;
import org.braekpo1nt.mctmanager.config.exceptions.ConfigInvalidException;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.braekpo1nt.mctmanager.games.game.enums.GameType;
import org.braekpo1nt.mctmanager.games.game.footrace.config.FootRaceConfig;
import org.braekpo1nt.mctmanager.games.game.footrace.config.FootRaceConfigController;
import org.braekpo1nt.mctmanager.games.game.interfaces.Configurable;
import org.braekpo1nt.mctmanager.games.game.interfaces.MCTGame;
import org.braekpo1nt.mctmanager.games.utils.ParticipantInitializer;
import org.braekpo1nt.mctmanager.ui.TimeStringUtils;
import org.braekpo1nt.mctmanager.ui.sidebar.Headerable;
import org.braekpo1nt.mctmanager.ui.sidebar.KeyLine;
import org.braekpo1nt.mctmanager.ui.sidebar.Sidebar;
import org.braekpo1nt.mctmanager.utils.BlockPlacementUtils;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.time.Duration;
import java.util.*;

/**
 * Handles all the Foot Race game logic.
 */
public class FootRaceGame implements Listener, MCTGame, Configurable, Headerable {
    
    private final int MAX_LAPS = 3;
    private final FootRaceConfigController configController;
    private FootRaceConfig config;// 3 second
    private static final long COOL_DOWN_TIME = 3000L;
    private Sidebar sidebar;
    private Sidebar adminSidebar;
    private boolean gameActive = false;
    private boolean raceHasStarted = false;
    /**
     * Holds the Foot Race world
     */
    private final Main plugin;
    private final GameManager gameManager;
    private int startCountDownTaskID;
    private int endRaceCountDownId;
    private int timerRefreshTaskId;
    private List<Player> participants;
    private List<Player> admins;
    private Map<UUID, Long> lapCooldowns;
    private Map<UUID, Integer> laps;
    private ArrayList<UUID> placements;
    private long raceStartTime;
    private final PotionEffect SPEED = new PotionEffect(PotionEffectType.SPEED, 10000, 8, true, false, false);
    private final PotionEffect INVISIBILITY = new PotionEffect(PotionEffectType.INVISIBILITY, 10000, 1, true, false, false);
    private int statusEffectsTaskId;
    private int descriptionPeriodTaskId;
    private boolean descriptionShowing = false;
    private final String title = ChatColor.BLUE+"Foot Race";
    
    public FootRaceGame(Main plugin, GameManager gameManager) {
        this.plugin = plugin;
        this.gameManager = gameManager;
        this.configController = new FootRaceConfigController(plugin.getDataFolder());
    }
    
    @Override
    public GameType getType() {
        return GameType.FOOT_RACE;
    }
    
    @Override
    public void loadConfig() throws ConfigIOException, ConfigInvalidException {
        this.config = configController.getConfig();
    }
    
    @Override
    public void start(List<Player> newParticipants, List<Player> newAdmins) {
        this.participants = new ArrayList<>(newParticipants.size());
        lapCooldowns = new HashMap<>(newParticipants.size());
        laps = new HashMap<>(newParticipants.size());
        placements = new ArrayList<>(newParticipants.size());
        admins = new ArrayList<>(newAdmins.size());
        sidebar = gameManager.getSidebarFactory().createSidebar();
        adminSidebar = gameManager.getSidebarFactory().createSidebar();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        closeGlassBarrier();
        for (Player participant : newParticipants) {
            initializeParticipant(participant);
        }
        startAdmins(newAdmins);
        initializeSidebar();
        gameActive = true;
        startStatusEffectsTask();
        setupTeamOptions();
        displayDescription();
        startDescriptionPeriod();
        Bukkit.getLogger().info("Starting Foot Race game");
    }
    
    private void displayDescription() {
        messageAllParticipants(config.getDescription());
    }
    
    private void initializeParticipant(Player participant) {
        UUID participantUniqueId = participant.getUniqueId();
        participants.add(participant);
        lapCooldowns.put(participantUniqueId, System.currentTimeMillis());
        laps.put(participantUniqueId, 1);
        sidebar.addPlayer(participant);
        participant.sendMessage("Teleporting to Foot Race");
        participant.teleport(config.getStartingLocation());
        participant.getInventory().clear();
        giveBoots(participant);
        participant.setGameMode(GameMode.ADVENTURE);
        ParticipantInitializer.clearStatusEffects(participant);
        ParticipantInitializer.resetHealthAndHunger(participant);
    }
    
    private void startAdmins(List<Player> newAdmins) {
        this.admins = new ArrayList<>(newAdmins.size());
        for (Player admin : newAdmins) {
            initializeAdmin(admin);
        }
        initializeAdminSidebar();
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
    
    private void initializeAdmin(Player admin) {
        admins.add(admin);
        adminSidebar.addPlayer(admin);
        admin.setGameMode(GameMode.SPECTATOR);
        admin.teleport(config.getStartingLocation());
    }
    
    @Override
    public void stop() {
        HandlerList.unregisterAll(this);
        closeGlassBarrier();
        cancelAllTasks();
        stopAdmins();
        for (Player participant : participants) {
            resetParticipant(participant);
        }
        clearSidebar();
        participants.clear();
        lapCooldowns.clear();
        laps.clear();
        placements.clear();
        raceHasStarted = false;
        descriptionShowing = false;
        gameActive = false;
        gameManager.gameIsOver();
        Bukkit.getLogger().info("Stopping Foot Race game");
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
    
    private void resetParticipant(Player participant) {
        participant.getInventory().clear();
        ParticipantInitializer.clearStatusEffects(participant);
        ParticipantInitializer.resetHealthAndHunger(participant);
        sidebar.removePlayer(participant);
    }
    
    @Override
    public void onParticipantJoin(Player participant) {
        if (!gameActive) {
            return;
        }
        if (participantShouldRejoin(participant)) {
            rejoinParticipant(participant);
        } else {
            initializeParticipant(participant);
        }
        sidebar.updateLine(participant.getUniqueId(), "title", title);
        
        Integer currentLap = laps.get(participant.getUniqueId());
        if (currentLap > MAX_LAPS) {
            showRaceCompleteFastBoard(participant.getUniqueId());
        } else {
            sidebar.updateLine(participant.getUniqueId(), "lap", String.format("Lap: %d/%d", currentLap, MAX_LAPS));
        }
    }
    
    /**
     * Run for a participant who was in the event, left, then rejoined.
     * @param participant The participant who is rejoining
     */
    private void rejoinParticipant(Player participant) {
        sidebar.addPlayer(participant);
        participant.sendMessage(ChatColor.YELLOW + "You have rejoined Foot Race");
        participants.add(participant);
        UUID uniqueId = participant.getUniqueId();
        if (placements.contains(uniqueId)) {
            showRaceCompleteFastBoard(uniqueId);
            return;
        }
        giveBoots(participant);
    }
    
    /**
     * Checks if the participant was previously in the game, and should thus rejoin
     * @param participant The participant to check
     * @return True if the participant was in the game before, and should rejoin. False
     * if the participant wasn't in the game before. 
     */
    private boolean participantShouldRejoin(Player participant) {
        UUID uniqueId = participant.getUniqueId();
        if (!raceHasStarted) {
            return false;
        }
        return placements.contains(uniqueId) || laps.containsKey(uniqueId);
    }
    
    @Override
    public void onParticipantQuit(Player participant) {
        if (!gameActive) {
            return;
        }
        resetParticipant(participant);
        participants.remove(participant);
    }
    
    private void cancelAllTasks() {
        Bukkit.getScheduler().cancelTask(startCountDownTaskID);
        Bukkit.getScheduler().cancelTask(endRaceCountDownId);
        Bukkit.getScheduler().cancelTask(timerRefreshTaskId);
        Bukkit.getScheduler().cancelTask(statusEffectsTaskId);
        Bukkit.getScheduler().cancelTask(descriptionPeriodTaskId);
    }
    
    private void giveBoots(Player participant) {
        Color teamColor = gameManager.getTeamColor(participant.getUniqueId());
        ItemStack boots = new ItemStack(Material.LEATHER_BOOTS);
        LeatherArmorMeta meta = (LeatherArmorMeta) boots.getItemMeta();
        meta.setColor(teamColor);
        boots.setItemMeta(meta);
        participant.getEquipment().setBoots(boots);
    }
    
    private void setupTeamOptions() {
        Scoreboard mctScoreboard = gameManager.getMctScoreboard();
        for (Team team : mctScoreboard.getTeams()) {
            team.setAllowFriendlyFire(false);
            team.setCanSeeFriendlyInvisibles(true);
            team.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.ALWAYS);
            team.setOption(Team.Option.DEATH_MESSAGE_VISIBILITY, Team.OptionStatus.ALWAYS);
            team.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.NEVER);
        }
    }
    
    private void startStatusEffectsTask() {
        this.statusEffectsTaskId = new BukkitRunnable(){
            @Override
            public void run() {
                for (Player participant : participants) {
                    participant.addPotionEffect(SPEED);
                    participant.addPotionEffect(INVISIBILITY);
                }
            }
        }.runTaskTimer(plugin, 0L, 60L).getTaskId();
    }
    
    private void startDescriptionPeriod() {
        descriptionShowing = true;
        this.descriptionPeriodTaskId = new BukkitRunnable() {
            private int count = config.getDescriptionDuration();
            @Override
            public void run() {
                if (count <= 0) {
                    sidebar.updateLine("timer", "");
                    adminSidebar.updateLine("timer", "");
                    descriptionShowing = false;
                    startStartRaceCountdownTask();
                    this.cancel();
                    return;
                }
                String timeLeft = TimeStringUtils.getTimeString(count);
                String timerString = String.format("Starting soon: %s", timeLeft);
                sidebar.updateLine("timer", timerString);
                adminSidebar.updateLine("timer", timerString);
                count--;
            }
        }.runTaskTimer(plugin, 0L, 20L).getTaskId();
    }
    
    private void startStartRaceCountdownTask() {
        this.startCountDownTaskID = new BukkitRunnable() {
            int count = config.getStartRaceDuration();
            
            @Override
            public void run() {
                if (count <= 0) {
                    messageAllParticipants(Component.text("Go!"));
                    sidebar.updateLine("timer", "");
                    adminSidebar.updateLine("timer", "");
                    startRace();
                    this.cancel();
                    return;
                }
                String timeLeft = TimeStringUtils.getTimeString(count);
                sidebar.updateLine("timer", String.format("Starting: %s", timeLeft));
                adminSidebar.updateLine("timer", String.format("Starting: %s", timeLeft));
                count--;
            }
        }.runTaskTimer(plugin, 0L, 20L).getTaskId();
    }
    
    private void startEndRaceCountDown() {
        this.endRaceCountDownId = new BukkitRunnable() {
            int count = config.getRaceEndCountdownDuration();
            @Override
            public void run() {
                if (count <= 0) {
                    sidebar.updateLine("timer", "");
                    adminSidebar.updateLine("timer", "");
                    stop();
                    this.cancel();
                    return;
                }
                String timeLeft = TimeStringUtils.getTimeString(count);
                sidebar.updateLine("timer", String.format("Ending: %s", timeLeft));
                adminSidebar.updateLine("timer", String.format("Ending: %s", timeLeft));
                count--;
            }
        }.runTaskTimer(plugin, 0L, 20L).getTaskId();
    }
    
    private void startTimerRefreshTask() {
        this.timerRefreshTaskId = new BukkitRunnable(){
            @Override
            public void run() {
                long elapsedTime = System.currentTimeMillis() - raceStartTime;
                String timeString = getTimeString(elapsedTime);
                for (Player participant : participants) {
                    if (!placements.contains(participant.getUniqueId())) {
                        sidebar.updateLine(
                                participant.getUniqueId(), 
                                "elapsedTime",
                                timeString
                        );
                    }
                }
                for (Player admin : admins) {
                    adminSidebar.updateLine(
                            admin.getUniqueId(),
                            "elapsedTime",
                            timeString
                    );
                }
            }
        }.runTaskTimer(plugin, 0, 1).getTaskId();
    }
    
    private void startRace() {
        openGlassBarrier();
        raceStartTime = System.currentTimeMillis();
        raceHasStarted = true;
        startTimerRefreshTask();
    }
    
    private void openGlassBarrier() {
        BlockPlacementUtils.createCubeReplace(config.getWorld(), config.getGlassBarrier(), Material.WHITE_STAINED_GLASS_PANE, Material.AIR);
    }
    
    private void closeGlassBarrier() {
        BlockPlacementUtils.createCubeReplace(config.getWorld(), config.getGlassBarrier(), Material.AIR, Material.WHITE_STAINED_GLASS_PANE);
        BlockPlacementUtils.updateDirection(config.getWorld(), config.getGlassBarrier());
    }
    
    private void initializeAdminSidebar() {
        adminSidebar.addLines(
                new KeyLine("title", title),
                new KeyLine("elapsedTime", "00:00:000"),
                new KeyLine("timer", "")
        );
    }
    
    private void clearAdminSidebar() {
        adminSidebar.deleteAllLines();
        adminSidebar = null;
    }
    
    private void initializeSidebar() {
        sidebar.addLines(
                new KeyLine("personalTeam", ""),
                // new KeyLine("personalScore", ""),
                new KeyLine("title", title),
                new KeyLine("elapsedTime", "00:00:000"),
                new KeyLine("lap", String.format("Lap: %d/%d", 1, MAX_LAPS)),
                new KeyLine("timer", "")
        );
    }
    
    private void clearSidebar() {
        sidebar.deleteAllLines();
        sidebar = null;
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
//        if (sidebar == null) {
//            return;
//        }
//        if (!participants.contains(participant)) {
//            return;
//        }
//        sidebar.updateLine(participant.getUniqueId(), "personalScore", contents);
    }
    
    private void showRaceCompleteFastBoard(UUID playerUUID) {
        long elapsedTime = System.currentTimeMillis() - raceStartTime;
        sidebar.updateLines(playerUUID, 
                new KeyLine("elapsedTime", getTimeString(elapsedTime)), 
                new KeyLine("lap", String.format("Finished %s!", getPlacementTitle(placements.indexOf(playerUUID) + 1)))
        );
    }
    
    @EventHandler
    public void onPlayerCrossFinishLine(PlayerMoveEvent event) {
        if (!gameActive) {
            return;
        }
        if (!raceHasStarted) {
            return;
        }
        Player player = event.getPlayer();
        UUID playerUUID = player.getUniqueId();
        if (!participants.contains(player)) {
            return;
        }
        if (!player.getWorld().equals(config.getWorld())) {
            return;
        }
        
        if (isInFinishLineBoundingBox(player)) {
            long lastMoveTime = lapCooldowns.get(playerUUID);
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastMoveTime < COOL_DOWN_TIME) {
                return;
            }
            lapCooldowns.put(playerUUID, currentTime);
            
            int currentLap = laps.get(playerUUID);
            if (currentLap < MAX_LAPS) {
                long elapsedTime = currentTime - raceStartTime;
                int newLap = currentLap + 1;
                laps.put(playerUUID, newLap);
                sidebar.updateLine(
                        playerUUID,
                        "lap",
                        String.format("Lap: %d/%d", laps.get(playerUUID), MAX_LAPS)
                );
                messageAllParticipants(Component.text(player.getName())
                        .append(Component.text(" finished lap "))
                        .append(Component.text(currentLap))
                        .append(Component.text(" in "))
                        .append(Component.text(getTimeString(elapsedTime))));
                gameManager.awardPointsToParticipant(player, config.getCompleteLapScore());
                return;
            }
            if (currentLap == MAX_LAPS) {
                laps.put(playerUUID, currentLap + 1);
                onPlayerFinishedRace(player);
            }
        }
    }
    
    /**
     * Stop players from removing their equipment
     */
    @EventHandler
    public void onClickInventory(InventoryClickEvent event) {
        if (!gameActive) {
            return;
        }
        if (event.getClickedInventory() == null) {
            return;
        }
        if (event.getCurrentItem() == null) {
            return;
        }
        Player participant = ((Player) event.getWhoClicked());
        if (!participants.contains(participant)) {
            return;
        }
        event.setCancelled(true);
    }
    
    /**
     * Stop players from dropping items
     */
    @EventHandler
    public void onDropItem(PlayerDropItemEvent event) {
        if (!gameActive) {
            return;
        }
        Player participant = event.getPlayer();
        if (!participants.contains(participant)) {
            return;
        }
        event.setCancelled(true);
    }
    
    @EventHandler
    public void onPlayerDamage(EntityDamageEvent event) {
        if (!gameActive) {
            return;
        }
        if (event.getCause().equals(EntityDamageEvent.DamageCause.VOID)) {
            return;
        }
        if (!(event.getEntity() instanceof Player participant)) {
            return;
        }
        if (!participants.contains(participant)) {
            return;
        }
        event.setCancelled(true);
    }
    
    @EventHandler
    public void onPlayerLoseHunger(FoodLevelChangeEvent event) {
        if (!(event.getEntity() instanceof Player participant)) {
            return;
        }
        if (!participants.contains(participant)) {
            return;
        }
        participant.setFoodLevel(20);
        event.setCancelled(true);
    }
    
    /**
     * Returns the given milliseconds as a string representing time in the format
     * MM:ss:mmm (or minutes:seconds:milliseconds)
     * @param timeMillis The time in milliseconds
     * @return Time string MM:ss:mmm
     */
    private String getTimeString(long timeMillis) {
        Duration duration = Duration.ofMillis(timeMillis);
        long minutes = duration.toMinutes();
        long seconds = duration.minusMinutes(minutes).getSeconds();
        long millis = duration.minusMinutes(minutes).minusSeconds(seconds).toMillis();
        return String.format("%d:%02d:%03d", minutes, seconds, millis);
    }
    
    /**
     * Code to run when a single player crosses the finish line for the last time
     * @param player The player who crossed the finish line
     */
    private void onPlayerFinishedRace(Player player) {
        long elapsedTime = System.currentTimeMillis() - raceStartTime;
        placements.add(player.getUniqueId());
        showRaceCompleteFastBoard(player.getUniqueId());
        int placement = placements.indexOf(player.getUniqueId()) + 1;
        int points = calculatePointsForPlacement(placement);
        gameManager.awardPointsToParticipant(player, points);
        String placementTitle = getPlacementTitle(placement);
        String timeString = getTimeString(elapsedTime);
        String endCountDown = TimeStringUtils.getTimeString(config.getRaceEndCountdownDuration());
        if (placements.size() == 1) {
            messageAllParticipants(Component.text(player.getName())
                    .append(Component.text(" finished 1st in "))
                    .append(Component.text(timeString))
                    .append(Component.text("! Only ")
                            .append(Component.text(endCountDown))
                            .append(Component.text(" remains!"))
                            .color(NamedTextColor.RED))
                    .color(NamedTextColor.GREEN));
            startEndRaceCountDown();
            return;
        }
        messageAllParticipants(Component.text(player.getName())
                .append(Component.text(" finished "))
                .append(Component.text(placementTitle))
                .append(Component.text(" in "))
                .append(Component.text(timeString)));
    }
    
    private void messageAllParticipants(Component message) {
        gameManager.messageAdmins(message);
        for (Player participant : participants) {
            participant.sendMessage(message);
        }
    }
    
    /**
     * Calculates the points to be awarded for the given placement. This is based on user-configured values. Returns a set number of values for placement less than or equal to x, and a detriment of 10 points for each successive placement greater than x
     * @param placement the placement number (1=1st place, 2=2nd place, 300=300th place) to get the points for. Must be 1 or more.
     * @return The number of points to award for the placement, no less than 0.
     */
    int calculatePointsForPlacement(int placement) {
        if (placement < 1) {
            throw new IllegalArgumentException("placement can't be less than 1");
        }
        int[] placementPoints = config.getPlacementPoints();
        if (placement <= placementPoints.length) {
            return placementPoints[placement-1];
        }
        int minPlacementPoints = placementPoints[placementPoints.length-1];
        int points = minPlacementPoints - ((placement-placementPoints.length) * config.getDetriment());
        return Math.max(points, 0);
    }
    
    /**
     * Returns the formal placement title of the given place. 
     * 1 gives 1st, 2 gives second, 11 gives 11th, 103 gives 103rd.
     * @param placement A number representing the placement
     * @return The placement number with the appropriate postfix (st, nd, rd, th)
     */
    private String getPlacementTitle(int placement) {
        if (placement % 100 >= 11 && placement % 100 <= 13) {
            return placement + "th";
        } else {
            return switch (placement % 10) {
                case 1 -> placement + "st";
                case 2 -> placement + "nd";
                case 3 -> placement + "rd";
                default -> placement + "th";
            };
        }
    }
    
    private boolean isInFinishLineBoundingBox(Player player) {
        return config.getFinishLine().contains(player.getLocation().toVector());
    }
}
