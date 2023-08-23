package org.braekpo1nt.mctmanager.games.game.footrace;

import com.onarandombox.MultiverseCore.api.MVWorldManager;
import com.onarandombox.MultiverseCore.utils.AnchorManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.braekpo1nt.mctmanager.games.game.enums.GameType;
import org.braekpo1nt.mctmanager.games.game.interfaces.MCTGame;
import org.braekpo1nt.mctmanager.games.utils.ParticipantInitializer;
import org.bukkit.*;
import org.bukkit.block.structure.Mirror;
import org.bukkit.block.structure.StructureRotation;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.*;
import org.bukkit.structure.Structure;
import org.bukkit.util.BoundingBox;

import java.time.Duration;
import java.util.*;

/**
 * Handles all the Foot Race game logic.
 */
public class FootRaceGame implements Listener, MCTGame {
    
    private final int MAX_LAPS = 3;
        
    private boolean gameActive = false;
    private boolean raceHasStarted = false;
    /**
     * Holds the Foot Race world
     */
    private final World footRaceWorld;
    private final BoundingBox finishLine = new BoundingBox(2396, 80, 295, 2404, 79, 308);
    private final Main plugin;
    private final GameManager gameManager;
    private int startCountDownTaskID;
    private int endRaceCountDownId;
    private int timerRefreshTaskId;
    private List<Player> participants;
    private Map<UUID, Long> lapCooldowns;
    private Map<UUID, Integer> laps;
    private ArrayList<UUID> placements;
    private long raceStartTime;
    private final PotionEffect SPEED = new PotionEffect(PotionEffectType.SPEED, 10000, 8, true, false, false);
    private final PotionEffect INVISIBILITY = new PotionEffect(PotionEffectType.INVISIBILITY, 10000, 1, true, false, false);
    private final PotionEffect RESISTANCE = new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 70, 200, true, false, false);
    private final PotionEffect REGENERATION = new PotionEffect(PotionEffectType.REGENERATION, 70, 200, true, false, false);
    private final PotionEffect FIRE_RESISTANCE = new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 70, 1, true, false, false);
    private final PotionEffect SATURATION = new PotionEffect(PotionEffectType.SATURATION, 70, 250, true, false, false);
    private int statusEffectsTaskId;
    private final String title = ChatColor.BLUE+"Foot Race";
    private Location footRaceStartAnchor;
    
    public FootRaceGame(Main plugin, GameManager gameManager) {
        this.plugin = plugin;
        this.gameManager = gameManager;
        MVWorldManager worldManager = Main.multiverseCore.getMVWorldManager();
        this.footRaceWorld = worldManager.getMVWorld("NT").getCBWorld();
    }
    
    @Override
    public GameType getType() {
        return GameType.FOOT_RACE;
    }
    
    @Override
    public void start(List<Player> newParticipants) {
        this.participants = new ArrayList<>();
        lapCooldowns = new HashMap<>();
        laps = new HashMap<>();
        placements = new ArrayList<>();
        AnchorManager anchorManager = Main.multiverseCore.getAnchorManager();
        this.footRaceStartAnchor = anchorManager.getAnchorLocation("foot-race");
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        closeGlassBarrier();
        for (Player participant : newParticipants) {
            initializeParticipant(participant);
        }
        startStatusEffectsTask();
        setupTeamOptions();
        startStartRaceCountdownTask();
        gameActive = true;
        Bukkit.getLogger().info("Starting Foot Race game");
    }
    
    private void initializeParticipant(Player participant) {
        UUID participantUniqueId = participant.getUniqueId();
        participants.add(participant);
        lapCooldowns.put(participantUniqueId, System.currentTimeMillis());
        laps.put(participantUniqueId, 1);
        initializeFastBoard(participant);
        participant.sendMessage("Teleporting to Foot Race");
        participant.teleport(footRaceStartAnchor);
        participant.getInventory().clear();
        giveBoots(participant);
        participant.setGameMode(GameMode.ADVENTURE);
        ParticipantInitializer.clearStatusEffects(participant);
        ParticipantInitializer.resetHealthAndHunger(participant);
    }
    
    @Override
    public void stop() {
        HandlerList.unregisterAll(this);
        closeGlassBarrier();
        cancelAllTasks();
        for (Player participant : participants) {
            resetParticipant(participant);
        }
        participants.clear();
        raceHasStarted = false;
        gameActive = false;
        gameManager.gameIsOver();
        Bukkit.getLogger().info("Stopping Foot Race game");
    }
    
    private void resetParticipant(Player participant) {
        participant.getInventory().clear();
        ParticipantInitializer.clearStatusEffects(participant);
        ParticipantInitializer.resetHealthAndHunger(participant);
        hideFastBoard(participant);
    }
    
    @Override
    public void onParticipantJoin(Player participant) {
        if (!gameActive) {
            return;
        }
        if (participantShouldRejoin(participant)) {
            rejoinParticipant(participant);
        } else {
            messageAllParticipants(Component.text(participant.getName())
                    .append(Component.text(" is joining Foot Race!"))
                    .color(NamedTextColor.YELLOW));
            initializeParticipant(participant);
        }
    }
    
    /**
     * Run for a participant who was in the event, left, then rejoined.
     * @param participant The participant who is rejoining
     */
    private void rejoinParticipant(Player participant) {
        participant.sendMessage(ChatColor.YELLOW + "You have rejoined Foot Race");
        messageAllParticipants(Component.text(participant.getName())
                .append(Component.text(" is rejoining the game!"))
                .color(NamedTextColor.YELLOW));
        participants.add(participant);
        UUID uniqueId = participant.getUniqueId();
        if (placements.contains(uniqueId)) {
            showRaceCompleteFastBoard(uniqueId);
            return;
        }
        initializeFastBoard(participant);
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
        messageAllParticipants(Component.text(participant.getName())
                .append(Component.text(" has left the game!"))
                .color(NamedTextColor.YELLOW));
        resetParticipant(participant);
        participants.remove(participant);
    }
    
    private void cancelAllTasks() {
        Bukkit.getScheduler().cancelTask(startCountDownTaskID);
        Bukkit.getScheduler().cancelTask(endRaceCountDownId);
        Bukkit.getScheduler().cancelTask(timerRefreshTaskId);
        Bukkit.getScheduler().cancelTask(statusEffectsTaskId);
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
                    participant.addPotionEffect(RESISTANCE);
                    participant.addPotionEffect(REGENERATION);
                    participant.addPotionEffect(FIRE_RESISTANCE);
                    participant.addPotionEffect(SATURATION);
                }
            }
        }.runTaskTimer(plugin, 0L, 60L).getTaskId();
    }
    
    private void startStartRaceCountdownTask() {
        this.startCountDownTaskID = new BukkitRunnable() {
            private int count = 10;
            
            @Override
            public void run() {
                for (Player participant : participants) {
                    if (count <= 0) {
                        participant.sendMessage(Component.text("Go!"));
                        
                    } else {
                        participant.sendMessage(Component.text(count));
                    }
                }
                if (count <= 0) {
                    startRace();
                    this.cancel();
                    return;
                }
                count--;
            }
        }.runTaskTimer(plugin, 0L, 20L).getTaskId();
    }
    
    private void startEndRaceCountDown() {
        this.endRaceCountDownId = new BukkitRunnable() {
            private int count = 60;
            @Override
            public void run() {
                if (count <= 0) {
                    this.cancel();
                    stop();
                    return;
                }
                if (count > 0) {
                    if (count <= 10) {
                        messageAllParticipants(Component.text("Ending in ")
                                .append(Component.text(count)));
                    }
                }
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
                        gameManager.getFastBoardManager().updateLine(
                                participant.getUniqueId(),
                                1,
                                timeString
                        );
                    }
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
        Structure structure = Bukkit.getStructureManager().loadStructure(new NamespacedKey("mctdatapack", "footrace/gateopen"));
        structure.place(new Location(footRaceWorld, 2397, 76, 317), true, StructureRotation.NONE, Mirror.NONE, 0, 1, new Random());
    }
    
    private void closeGlassBarrier() {
        Structure structure = Bukkit.getStructureManager().loadStructure(new NamespacedKey("mctdatapack", "footrace/gateclosed"));
        structure.place(new Location(footRaceWorld, 2397, 76, 317), true, StructureRotation.NONE, Mirror.NONE, 0, 1, new Random());
    }
    
    private void initializeFastBoard(Player participant) {
        gameManager.getFastBoardManager().updateLines(
                participant.getUniqueId(),
                title,
                "00:00:000",
                "",
                String.format("Lap: %d/%d", laps.get(participant.getUniqueId()), MAX_LAPS),
                ""
        );
    }
    
    private void hideFastBoard(Player participant) {
        gameManager.getFastBoardManager().updateLines(
                participant.getUniqueId()
        );
    }
    
    private void updateFastBoard(Player participant) {
        long elapsedTime = System.currentTimeMillis() - raceStartTime;
        gameManager.getFastBoardManager().updateLines(
                participant.getUniqueId(),
                title,
                getTimeString(elapsedTime),
                "",
                String.format("Lap: %d/%d", laps.get(participant.getUniqueId()), MAX_LAPS),
                ""
        );
    }
    
    private void showRaceCompleteFastBoard(UUID playerUniqueId) {
        long elapsedTime = System.currentTimeMillis() - raceStartTime;
        gameManager.getFastBoardManager().updateLines(
                playerUniqueId,
                title,
                getTimeString(elapsedTime),
                "",
                "Race Complete!",
                getPlacementTitle(placements.indexOf(playerUniqueId) + 1),
                ""
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
        if (!player.getWorld().equals(footRaceWorld)) {
            return;
        }
        
        if (isInFinishLineBoundingBox(player)) {
            long lastMoveTime = lapCooldowns.get(playerUUID);
            long currentTime = System.currentTimeMillis();
            long coolDownTime = 3000L; // 3 second
            if (currentTime - lastMoveTime < coolDownTime) {
                //Not enough time has elapsed, return without doing anything
                return;
            }
            lapCooldowns.put(playerUUID, System.currentTimeMillis());
            
            int currentLap = laps.get(playerUUID);
            if (currentLap < MAX_LAPS) {
                long elapsedTime = System.currentTimeMillis() - raceStartTime;
                int newLap = currentLap + 1;
                laps.put(playerUUID, newLap);
                updateFastBoard(player);
                messageAllParticipants(Component.text(player.getName())
                        .append(Component.text(" finished lap "))
                        .append(Component.text(currentLap))
                        .append(Component.text(" in "))
                        .append(Component.text(getTimeString(elapsedTime))));
                return;
            }
            if (currentLap == MAX_LAPS) {
                laps.put(playerUUID, currentLap + 1);
                onPlayerFinishedRace(player);
            }
        }
    }
    
    /**
     * Returns the given milliseconds as a string representing time in the format
     * MM:ss:mmm (or minutes:seconds:milliseconds)
     * @param timeMilis The time in milliseconds
     * @return Time string MM:ss:mmm
     */
    private String getTimeString(long timeMilis) {
        Duration duration = Duration.ofMillis(timeMilis);
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
        if (placements.size() == 1) {
            messageAllParticipants(Component.text(player.getName())
                    .append(Component.text(" finished 1st in "))
                    .append(Component.text(timeString))
                    .append(Component.text("! Only 1 minute remains!")
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
    
    private int calculatePointsForPlacement(int placement) {
        switch (placement) {
            case 1:
                return 350;
            case 2:
                return 275;
            case 3:
                return 200;
            case 4:
                return 170;
            case 5:
                return 150;
            default:
                int previousPoints = calculatePointsForPlacement(placement - 1);
                int points = previousPoints - 10;
                return Math.max(points, 0);
        }
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
            switch (placement % 10) {
                case 1:
                    return placement + "st";
                case 2:
                    return placement + "nd";
                case 3:
                    return placement + "rd";
                default:
                    return placement + "th";
            }
        }
    }
    
    private boolean isInFinishLineBoundingBox(Player player) {
        return finishLine.contains(player.getLocation().toVector());
    }
}
