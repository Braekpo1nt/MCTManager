package org.braekpo1nt.mctmanager.games.parkourpathway;

import com.onarandombox.MultiverseCore.utils.AnchorManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.braekpo1nt.mctmanager.games.enums.MCTGames;
import org.braekpo1nt.mctmanager.games.interfaces.MCTGame;
import org.braekpo1nt.mctmanager.games.parkourpathway.config.ParkourPathwayStorageUtil;
import org.braekpo1nt.mctmanager.games.utils.ParticipantInitializer;
import org.braekpo1nt.mctmanager.ui.TimeStringUtils;
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

import java.io.IOException;
import java.util.*;

public class ParkourPathwayGame implements MCTGame, Listener {

    private final Main plugin;
    private final GameManager gameManager;
    private final String title = ChatColor.BLUE+"Parkour Pathway";
    private final PotionEffect INVISIBILITY = new PotionEffect(PotionEffectType.INVISIBILITY, 10000, 1, true, false, false);
    private final PotionEffect RESISTANCE = new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 70, 200, true, false, false);
    private final PotionEffect REGENERATION = new PotionEffect(PotionEffectType.REGENERATION, 70, 200, true, false, false);
    private final PotionEffect FIRE_RESISTANCE = new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 70, 1, true, false, false);
    private final PotionEffect SATURATION = new PotionEffect(PotionEffectType.SATURATION, 70, 250, true, false, false);
    private int statusEffectsTaskId;
    private int startNextRoundTimerTaskId;
    private int checkpointCounterTask;
    private boolean gameActive = false;
    private List<Player> participants;
    private final Location parkourPathwayStartAnchor;
    private final List<CheckPoint> checkpoints;
    /**
     * UUID paired with index of checkpoint
     */
    private Map<UUID, Integer> currentCheckpoints;
    
    public ParkourPathwayGame(Main plugin, GameManager gameManager) {
        this.plugin = plugin;
        this.gameManager = gameManager;
        AnchorManager anchorManager = Main.multiverseCore.getAnchorManager();
        this.parkourPathwayStartAnchor = anchorManager.getAnchorLocation("parkour-pathway");
        this.checkpoints = loadCheckpoints();
    }
    
    @Override
    public MCTGames getType() {
        return MCTGames.PARKOUR_PATHWAY;
    }
    
    @Override
    public void start(List<Player> newParticipants) {
        participants = new ArrayList<>();
        currentCheckpoints = new HashMap<>();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        for (Player participant : newParticipants) {
            initializeParticipant(participant);
        }
        startStatusEffectsTask();
        setupTeamOptions();
        startParkourPathwayTimer();
        restartCheckpointCounter();
        gameActive = true;
        Bukkit.getLogger().info("Starting Parkour Pathway game");
    }

    private void initializeParticipant(Player participant) {
        UUID participantUniqueId = participant.getUniqueId();
        participants.add(participant);
        currentCheckpoints.put(participantUniqueId, 0);
        initializeFastBoard(participant);
        teleportPlayerToStartingPosition(participant);
        participant.getInventory().clear();
        giveBoots(participant);
        participant.setGameMode(GameMode.ADVENTURE);
        ParticipantInitializer.clearStatusEffects(participant);
        ParticipantInitializer.resetHealthAndHunger(participant);
    }

    private void resetParticipant(Player participant) {
        participant.getInventory().clear();
        hideFastBoard(participant);
    }
    
    @Override
    public void stop() {
        HandlerList.unregisterAll(this);
        cancelAllTasks();
        for (Player participant : participants) {
            resetParticipant(participant);
        }
        participants.clear();
        gameActive = false;
        gameManager.gameIsOver();
        Bukkit.getLogger().info("Stopping Parkour Pathway game");
    }

    @Override
    public void onParticipantJoin(Player participant) {
        
    }

    @Override
    public void onParticipantQuit(Player participant) {
        
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
        if (!currentCheckpoints.containsKey(playerUUID)) {
            currentCheckpoints.put(playerUUID, 0);
        }
        int currentCheckpointIndex = currentCheckpoints.get(playerUUID);
        int nextCheckpointIndex = currentCheckpointIndex + 1;
        if (nextCheckpointIndex >= checkpoints.size()) {
            // the player is in the finish line and already won
            return;
        }
        CheckPoint nextCheckpoint = checkpoints.get(nextCheckpointIndex);
        if (nextCheckpoint.boundingBox().contains(player.getLocation().toVector())) {
            // Player got to the next checkpoint
            currentCheckpoints.put(playerUUID, nextCheckpointIndex);
            updateCheckpointFastBoard(player);
            if (nextCheckpointIndex >= checkpoints.size()) {
                messageAllParticipants(Component.empty()
                        .append(Component.text(player.getName()))
                    .append(Component.text(" finished!")));
                int points = calculatePointsForWin(playerUUID);
                gameManager.awardPointsToPlayer(player, points);
            } else {
                messageAllParticipants(Component.empty()
                        .append(Component.text(player.getName()))
                        .append(Component.text(" reached checkpoint "))
                        .append(Component.text(nextCheckpointIndex))
                        .append(Component.text("/"))
                        .append(Component.text(checkpoints.size()-1)));
                int points = calculatePointsForCheckpoint(playerUUID);
                gameManager.awardPointsToPlayer(player, points);
            }
            if (allPlayersHaveFinished()) {
                stop();
                return;
            }
            restartCheckpointCounter();
            return;
        }
        CheckPoint currentCheckpoint = checkpoints.get(currentCheckpointIndex);
        double yPos = player.getLocation().getY();
        if (yPos < currentCheckpoint.yValue()) {
            // Player fell, and must be teleported to checkpoint spawn
            player.teleport(currentCheckpoint.respawn());
        }
    }

    private boolean allPlayersHaveFinished() {
        for (Player participant : participants) {
            int currentCheckpoint = currentCheckpoints.get(participant.getUniqueId());
            if (currentCheckpoint < checkpoints.size() - 1) {
                //at least one player is still playing
                return false;
            }
        }
        //all players are at finish line
        return true;
    }

    private int calculatePointsForCheckpoint(UUID playerUUID) {
        int playersCheckpoint = currentCheckpoints.get(playerUUID);
        int count = 0;
        for (int checkpointIndex : currentCheckpoints.values()) {
            if (checkpointIndex >= playersCheckpoint) {
                count++;
            }
        }
        switch (count) {
            case 1 -> {
                return 50;
            }
            case 2 -> {
                return 45;
            }
            case 3 -> {
                return 40;
            }
            default -> {
                return 35;
            }
        }
    }
    
    private int calculatePointsForWin(UUID playerUUID) {
        int numberOfWins = 0;
        for (int checkpointIndex : currentCheckpoints.values()) {
            if (checkpointIndex >= checkpoints.size() - 1) {
                numberOfWins++;
            }
        }
        switch (numberOfWins) {
            case 1 -> {
                return 400;
            }
            case 2 -> {
                return 300;
            }
            case 3 -> {
                return 200;
            }
            default -> {
                return 60;
            }
        }
    }
    
    private void restartCheckpointCounter() {
        Bukkit.getScheduler().cancelTask(this.checkpointCounterTask);
        for (Player participant : participants){
            resetCheckpointFastBoardTimer(participant);
        }
        this.checkpointCounterTask = new BukkitRunnable() {
            int count = 2*60;
            @Override
            public void run() {
                if (count <= 0) {
                    messageAllParticipants(Component.text("No one has reached a new checkpoint in the last two minutes. Stopping early."));
                    stop();
                    this.cancel();
                    return;
                }
                if (count == 30) {
                    messageAllParticipants(Component.empty()
                            .append(Component.text("No one has reached a new checkpoint in the last 1.5 minutes. Ending in 30 seconds"))
                            .color(NamedTextColor.RED));
                }
                if (count <= 30) {
                    String timeString = TimeStringUtils.getTimeString(count);
                    for (Player participant : participants){
                        updateCheckpointFastBoardTimer(participant, timeString);
                    }
                }
                count--;
            }
        }.runTaskTimer(plugin, 0L, 20L).getTaskId();
    }

    private void startParkourPathwayTimer() {
        this.startNextRoundTimerTaskId = new BukkitRunnable() {
            int count = 10*60;
            @Override
            public void run() {
                if (count <= 0) {
                    stop();
                    this.cancel();
                    return;
                }
                String timeString = TimeStringUtils.getTimeString(count);
                for (Player participant : participants){
                    updateParkourPathwayFastBoardTimer(participant, timeString);
                }
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
        player.teleport(parkourPathwayStartAnchor);
    }
    
    private void cancelAllTasks() {
        Bukkit.getScheduler().cancelTask(statusEffectsTaskId);
        Bukkit.getScheduler().cancelTask(startNextRoundTimerTaskId);
        Bukkit.getScheduler().cancelTask(checkpointCounterTask);
    }

    private void initializeFastBoard(Player participant) {
        gameManager.getFastBoardManager().updateLines(
                participant.getUniqueId(),
                title,
                "7:00",
                "",
                "1/" + (checkpoints.size()-1),
                "",
                "",
                ""
        );
    }
    
    private void updateParkourPathwayFastBoardTimer(Player participant, String timerString) {
        gameManager.getFastBoardManager().updateLine(
                participant.getUniqueId(),
                1,
                timerString
        );
    }
    
    private void updateCheckpointFastBoardTimer(Player participant, String timerString) {
        gameManager.getFastBoardManager().updateLine(
                participant.getUniqueId(),
                5,
                ChatColor.RED+"Ending in:"
        );
        gameManager.getFastBoardManager().updateLine(
                participant.getUniqueId(),
                6,
                ChatColor.RED+timerString
        );
    }
    
    private void resetCheckpointFastBoardTimer(Player participant) {
        gameManager.getFastBoardManager().updateLine(
                participant.getUniqueId(),
                5,
                ""
        );
        gameManager.getFastBoardManager().updateLine(
                participant.getUniqueId(),
                6,
                ""
        );
    }
    
    private void updateCheckpointFastBoard(Player participant) {
        int currentCheckpoint = currentCheckpoints.get(participant.getUniqueId());
        int lastCheckpoint = checkpoints.size()-1;
        gameManager.getFastBoardManager().updateLine(
                participant.getUniqueId(),
                3,
                currentCheckpoint + "/" + lastCheckpoint
        );
    }

    private void hideFastBoard(Player participant) {
        gameManager.getFastBoardManager().updateLines(
                participant.getUniqueId()
        );
    }

    private void messageAllParticipants(Component message) {
        gameManager.messageAdmins(message);
        for (Player participant : participants) {
            participant.sendMessage(message);
        }
    }
    
    private List<CheckPoint> loadCheckpoints() {
        ParkourPathwayStorageUtil parkourPathwayStorageUtil = new ParkourPathwayStorageUtil(plugin);
        try {
            parkourPathwayStorageUtil.loadConfig();
        } catch (IOException e) {
            Bukkit.getLogger().severe("Error loading parkour pathway checkpoints from config file. See console for details.");
            Bukkit.getPluginManager().disablePlugin(plugin);
            throw new RuntimeException(e);
        }
        return parkourPathwayStorageUtil.getCheckPoints();
    }
}
