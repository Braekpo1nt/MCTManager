package org.braekpo1nt.mctmanager.games.game.parkourpathway;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.braekpo1nt.mctmanager.games.game.enums.GameType;
import org.braekpo1nt.mctmanager.games.game.interfaces.Configurable;
import org.braekpo1nt.mctmanager.games.game.interfaces.MCTGame;
import org.braekpo1nt.mctmanager.games.game.parkourpathway.config.ParkourPathwayStorageUtil;
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
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.*;

public class ParkourPathwayGame implements MCTGame, Configurable, Listener, Headerable {

    private final Main plugin;
    private final GameManager gameManager;
    private Sidebar sidebar;
    private Sidebar adminSidebar;
    private final ParkourPathwayStorageUtil storageUtil;
    private final String title = ChatColor.BLUE+"Parkour Pathway";
    private final PotionEffect INVISIBILITY = new PotionEffect(PotionEffectType.INVISIBILITY, 10000, 1, true, false, false);
    private final PotionEffect RESISTANCE = new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 70, 200, true, false, false);
    private final PotionEffect REGENERATION = new PotionEffect(PotionEffectType.REGENERATION, 70, 200, true, false, false);
    private final PotionEffect FIRE_RESISTANCE = new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 70, 1, true, false, false);
    private final PotionEffect SATURATION = new PotionEffect(PotionEffectType.SATURATION, 70, 250, true, false, false);
    private int statusEffectsTaskId;
    private int startNextRoundTimerTaskId;
    private int checkpointCounterTask;
    private int startParkourPathwayTaskId;
    private boolean gameActive = false;
    private List<Player> participants = new ArrayList<>();
    private List<Player> admins = new ArrayList<>();
    /**
     * Participants who have reached the finish line
     */
    private List<UUID> finishedParticipants;
    /**
     * UUID paired with index of checkpoint
     */
    private Map<UUID, Integer> currentCheckpoints;
    
    public ParkourPathwayGame(Main plugin, GameManager gameManager) {
        this.plugin = plugin;
        this.gameManager = gameManager;
        storageUtil = new ParkourPathwayStorageUtil(plugin.getDataFolder());
    }
    
    @Override
    public GameType getType() {
        return GameType.PARKOUR_PATHWAY;
    }
    
    @Override
    public boolean loadConfig() throws IllegalArgumentException {
        return storageUtil.loadConfig();
    }
    
    @Override
    public void start(List<Player> newParticipants, List<Player> newAdmins) {
        participants = new ArrayList<>();
        currentCheckpoints = new HashMap<>();
        finishedParticipants = new ArrayList<>();
        closeGlassBarriers();
        sidebar = gameManager.getSidebarFactory().createSidebar();
        adminSidebar = gameManager.getSidebarFactory().createSidebar();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        for (Player participant : newParticipants) {
            initializeParticipant(participant);
        }
        initializeSidebar();
        startAdmins(newAdmins);
        startStatusEffectsTask();
        setupTeamOptions();
        startStartGameCountDown();
        gameActive = true;
        Bukkit.getLogger().info("Starting Parkour Pathway game");
    }
    
    private void initializeParticipant(Player participant) {
        participants.add(participant);
        currentCheckpoints.put(participant.getUniqueId(), 0);
        sidebar.addPlayer(participant);
        teleportPlayerToStartingPosition(participant);
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
    
    private void initializeAdmin(Player admin) {
        admins.add(admin);
        adminSidebar.addPlayer(admin);
        admin.setGameMode(GameMode.SPECTATOR);
        admin.teleport(storageUtil.getStartingLocation());
    }
    
    @Override
    public void stop() {
        HandlerList.unregisterAll(this);
        cancelAllTasks();
        for (Player participant : participants) {
            resetParticipant(participant);
        }
        clearSidebar();
        stopAdmins();
        participants.clear();
        finishedParticipants.clear();
        gameActive = false;
        gameManager.gameIsOver();
        Bukkit.getLogger().info("Stopping Parkour Pathway game");
    }
    
    private void resetParticipant(Player participant) {
        participant.getInventory().clear();
        sidebar.removePlayer(participant.getUniqueId());
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
    
    @Override
    public void onParticipantJoin(Player participant) {
        participants.add(participant);
        currentCheckpoints.putIfAbsent(participant.getUniqueId(), 0);
        sidebar.addPlayer(participant);
        participant.getInventory().clear();
        giveBoots(participant);
        participant.setGameMode(GameMode.ADVENTURE);
        ParticipantInitializer.clearStatusEffects(participant);
        ParticipantInitializer.resetHealthAndHunger(participant);
        sidebar.updateLine(participant.getUniqueId(), "title", title);
        updateCheckpointSidebar(participant);
    }
    
    @Override
    public void onParticipantQuit(Player participant) {
        resetParticipant(participant);
        participants.remove(participant);
    }
    
    private void startStartGameCountDown() {
        this.startParkourPathwayTaskId = new BukkitRunnable() {
            int count = storageUtil.getStartingDuration();
            @Override
            public void run() {
                if (count <= 0) {
                    messageAllParticipants(Component.text("Go!"));
                    openGlassBarriers();
                    startParkourPathwayTimer();
                    restartCheckpointCounter();
                    this.cancel();
                    return;
                }
                String timeLeft = TimeStringUtils.getTimeString(count);
                String timer = String.format("Starting: %s", timeLeft);
                sidebar.updateLine("timer", timer);
                adminSidebar.updateLine("timer", timer);
                count--;
            }
        }.runTaskTimer(plugin, 0L, 20L).getTaskId();
    }
    
    private void closeGlassBarriers() {
        BlockPlacementUtils.createCube(storageUtil.getWorld(), 1006, 0, -6, 1, 5, 13, Material.GLASS);
        BlockPlacementUtils.updateDirection(storageUtil.getWorld(), 1006, 0, -6, 1, 5, 13);
    }
    
    private void openGlassBarriers() {
        BlockPlacementUtils.createCube(storageUtil.getWorld(), 1006, 0, -6, 1, 5, 13, Material.AIR);
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
    public void onPlayerMove(PlayerMoveEvent event) {
        if (!gameActive) {
            return;
        }
        Player player = event.getPlayer();
        UUID playerUUID = player.getUniqueId();
        if (!participants.contains(player)) {
            return;
        }
        if (finishedParticipants.contains(playerUUID)) {
            return;
        }
        if (!currentCheckpoints.containsKey(playerUUID)) {
            currentCheckpoints.put(playerUUID, 0);
        }
        int currentCheckpointIndex = currentCheckpoints.get(playerUUID);
        int nextCheckpointIndex = currentCheckpointIndex + 1;
        if (nextCheckpointIndex >= storageUtil.getCheckPoints().size()) {
            // the player is in the finish line and already won
            return;
        }
        CheckPoint nextCheckpoint = storageUtil.getCheckPoints().get(nextCheckpointIndex);
        if (nextCheckpoint.boundingBox().contains(player.getLocation().toVector())) {
            // Player got to the next checkpoint
            currentCheckpoints.put(playerUUID, nextCheckpointIndex);
            updateCheckpointSidebar(player);
            if (nextCheckpointIndex >= storageUtil.getCheckPoints().size()-1) {
                onParticipantFinish(player);
            } else {
                messageAllParticipants(Component.empty()
                        .append(Component.text(player.getName()))
                        .append(Component.text(" reached checkpoint "))
                        .append(Component.text(nextCheckpointIndex))
                        .append(Component.text("/"))
                        .append(Component.text(storageUtil.getCheckPoints().size()-1)));
                int playersCheckpoint = currentCheckpoints.get(playerUUID);
                int points = calculatePointsForCheckpoint(playersCheckpoint, storageUtil.getCheckpointScore());
                gameManager.awardPointsToParticipant(player, points);
            }
            if (allPlayersHaveFinished()) {
                stop();
                return;
            }
            restartCheckpointCounter();
            return;
        }
        CheckPoint currentCheckpoint = storageUtil.getCheckPoints().get(currentCheckpointIndex);
        double yPos = player.getLocation().getY();
        if (yPos < currentCheckpoint.yValue()) {
            // Player fell, and must be teleported to checkpoint spawn
            player.teleport(currentCheckpoint.respawn().setDirection(player.getLocation().getDirection()));
        }
    }
    
    private void onParticipantFinish(Player participant) {
        messageAllParticipants(Component.empty()
                .append(Component.text(participant.getName()))
                .append(Component.text(" finished!")));
        int points = calculatePointsForWin(storageUtil.getWinScore());
        gameManager.awardPointsToParticipant(participant, points);
        participant.setGameMode(GameMode.SPECTATOR);
        finishedParticipants.add(participant.getUniqueId());
    }
    
    private boolean allPlayersHaveFinished() {
        for (Player participant : participants) {
            int currentCheckpoint = currentCheckpoints.get(participant.getUniqueId());
            if (currentCheckpoint < storageUtil.getCheckPoints().size() - 1) {
                //at least one player is still playing
                return false;
            }
        }
        //all players are at finish line
        return true;
    }
    
    /**
     * Calculates the points for playersCheckpoint based on how many players have reached or passed that playersCheckpoint. If checkpointScores has x elements, the nth player to arrive at playersCheckpoint gets the checkpointScores[n-1], unless n is greater than or equal to x, in which case they get checkpointScores[x-1]
     * @param playersCheckpoint the checkpoint to get the points for
     * @param checkpointScores the scores to progress through. The last score is to give to everyone who didn't make the one of the other specified scores.
     * @return the points for playersCheckpoint
     */
    private int calculatePointsForCheckpoint(int playersCheckpoint, int[] checkpointScores) {
        int numWhoReachedOrPassedCheckpoint = 0;
        for (int checkpointIndex : currentCheckpoints.values()) {
            if (checkpointIndex >= playersCheckpoint) {
                numWhoReachedOrPassedCheckpoint++;
            }
        }
        if (numWhoReachedOrPassedCheckpoint < checkpointScores.length) {
            return checkpointScores[numWhoReachedOrPassedCheckpoint - 1];
        } else {
            return checkpointScores[checkpointScores.length - 1];
        }
    }
    
    /**
     * Calculates the number of points for a win, based on how many players have currently won. If winScores has x elements, the nth player to win will get winScores[n-1] points, unless n is greater than or equal to x in which case they get winScores[x-1]
     * @param winScores the scores to progress through. The last score is to give to everyone who didn't make one of the other specified scores. 
     * @return the points for the most recent player win
     */
    private int calculatePointsForWin(int[] winScores) {
        int numberOfWins = 0;
        for (int checkpointIndex : currentCheckpoints.values()) {
            if (checkpointIndex >= storageUtil.getCheckPoints().size() - 1) {
                numberOfWins++;
            }
        }
        if (numberOfWins < winScores.length) {
            return winScores[numberOfWins - 1];
        } else {
            return winScores[winScores.length - 1];
        }
    }
    
    private void restartCheckpointCounter() {
        Bukkit.getScheduler().cancelTask(this.checkpointCounterTask);
        sidebar.updateLine("ending", "");
        adminSidebar.updateLine("ending", "");
        int checkpointCounter = storageUtil.getCheckpointCounterDuration();
        int checkpointCounterAlert = storageUtil.getCheckpointCounterAlertDuration();
        this.checkpointCounterTask = new BukkitRunnable() {
            int count = checkpointCounter;
            @Override
            public void run() {
                if (count <= 0) {
                    String timeString = TimeStringUtils.getTimeString(checkpointCounter);
                    messageAllParticipants(Component.text("No one has reached a new checkpoint in the last ")
                            .append(Component.text(timeString))
                            .append(Component.text(". Stopping early")));
                    stop();
                    this.cancel();
                    return;
                }
                if (count == checkpointCounterAlert) {
                    String timeString = TimeStringUtils.getTimeString(checkpointCounter);
                    messageAllParticipants(Component.text("No one has reached a new checkpoint in the last ")
                            .append(Component.text(timeString))
                            .append(Component.text(". Ending in "))
                            .append(Component.text(checkpointCounterAlert))
                            .append(Component.text("."))
                            .color(NamedTextColor.RED));
                }
                if (count <= checkpointCounterAlert) {
                    String timeString = TimeStringUtils.getTimeString(count);
                    String ending = String.format("%sEnding in: %s", ChatColor.RED, timeString);
                    sidebar.updateLine("ending", ending);
                    adminSidebar.updateLine("ending", ending);
                }
                count--;
            }
        }.runTaskTimer(plugin, 0L, 20L).getTaskId();
    }

    private void startParkourPathwayTimer() {
        int timeLimit = storageUtil.getTimeLimitDuration();
        this.startNextRoundTimerTaskId = new BukkitRunnable() {
            int count = timeLimit;
            @Override
            public void run() {
                if (count <= 0) {
                    stop();
                    this.cancel();
                    return;
                }
                String timeString = TimeStringUtils.getTimeString(count);
                sidebar.updateLine("timer", timeString);
                adminSidebar.updateLine("timer", timeString);
                count--;
            }
        }.runTaskTimer(plugin, 0L, 20L).getTaskId();
    }
    
    private void startStatusEffectsTask() {
        this.statusEffectsTaskId = new BukkitRunnable(){
            @Override
            public void run() {
                for (Player participant : participants) {
                    participant.addPotionEffect(INVISIBILITY);
                    participant.addPotionEffect(RESISTANCE);
                    participant.addPotionEffect(REGENERATION);
                    participant.addPotionEffect(FIRE_RESISTANCE);
                    participant.addPotionEffect(SATURATION);
                }
            }
        }.runTaskTimer(plugin, 0L, 60L).getTaskId();
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


    private void teleportPlayerToStartingPosition(Player player) {
        player.sendMessage("Teleporting to Parkour Pathway");
        player.teleport(storageUtil.getStartingLocation());
    }
    
    private void cancelAllTasks() {
        Bukkit.getScheduler().cancelTask(statusEffectsTaskId);
        Bukkit.getScheduler().cancelTask(startNextRoundTimerTaskId);
        Bukkit.getScheduler().cancelTask(checkpointCounterTask);
        Bukkit.getScheduler().cancelTask(startParkourPathwayTaskId);
    }
    
    private void initializeAdminSidebar() {
        adminSidebar.addLines(
                new KeyLine("title", title),
                new KeyLine("timer", ""),
                new KeyLine("ending", "")
        );
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
                new KeyLine("timer", ""),
                new KeyLine("checkpoint", String.format("0/%s", storageUtil.getCheckPoints().size() - 1)),
                new KeyLine("ending", "")
        );
    }
    
    private void updateCheckpointSidebar(Player participant) {
        int currentCheckpoint = currentCheckpoints.get(participant.getUniqueId());
        int lastCheckpoint = storageUtil.getCheckPoints().size()-1;
        sidebar.updateLine(participant.getUniqueId(), "checkpoint", String.format("%s/%s", currentCheckpoint, lastCheckpoint));
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
        if (sidebar == null) {
            return;
        }
        if (!participants.contains(participant)) {
            return;
        }
        sidebar.updateLine(participant.getUniqueId(), "personalScore", contents);
    }

    private void messageAllParticipants(Component message) {
        gameManager.messageAdmins(message);
        for (Player participant : participants) {
            participant.sendMessage(message);
        }
    }
}
