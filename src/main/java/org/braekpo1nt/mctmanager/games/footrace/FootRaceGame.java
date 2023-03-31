package org.braekpo1nt.mctmanager.games.footrace;

import com.onarandombox.MultiverseCore.api.MVWorldManager;
import com.onarandombox.MultiverseCore.utils.AnchorManager;
import fr.mrmicky.fastboard.FastBoard;
import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.braekpo1nt.mctmanager.games.MCTGame;
import org.bukkit.*;
import org.bukkit.block.structure.Mirror;
import org.bukkit.block.structure.StructureRotation;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
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
import java.util.stream.Collectors;

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
    private final Map<UUID, FastBoard> boards = new HashMap<>();
    private final PotionEffect SPEED = new PotionEffect(PotionEffectType.SPEED, 10000, 8, true, false, false);
    private final PotionEffect INVISIBILITY = new PotionEffect(PotionEffectType.INVISIBILITY, 10000, 1, true, false, false);
    private final PotionEffect RESISTANCE = new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 70, 200, true, false, false);
    private final PotionEffect REGENERATION = new PotionEffect(PotionEffectType.REGENERATION, 70, 200, true, false, false);
    private final PotionEffect FIRE_RESISTANCE = new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 70, 1, true, false, false);
    private final PotionEffect SATURATION = new PotionEffect(PotionEffectType.SATURATION, 70, 250, true, false, false);
    private int statusEffectsTaskId;
    
    public FootRaceGame(Main plugin, GameManager gameManager) {
        this.plugin = plugin;
        this.gameManager = gameManager;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        MVWorldManager worldManager = Main.multiverseCore.getMVWorldManager();
        this.footRaceWorld = worldManager.getMVWorld("NT").getCBWorld();
    }
    
    @Override
    public void start(List<Player> participants) {
        this.participants = participants;
        lapCooldowns = participants.stream().collect(
                Collectors.toMap(Entity::getUniqueId, value -> System.currentTimeMillis()));
        laps = participants.stream().collect(Collectors.toMap(Entity::getUniqueId, value -> 1));
        placements = new ArrayList<>();
        initializeFastBoards();
        closeGlassBarrier();
        teleportPlayersToStartingPositions();
        giveBoots();
        clearInventories();
        setPlayersToAdventure();
        clearStatusEffects();
        startStatusEffectsTask();
        startStartRaceCountdownTask();
        setupTeamOptions();
        gameActive = true;
        Bukkit.getLogger().info("Starting Foot Race game");
    }
    
    @Override
    public void stop() {
        closeGlassBarrier();
        hideFastBoards();
        cancelAllTasks();
        clearInventories();
        raceHasStarted = false;
        gameActive = false;
        gameManager.gameIsOver();
        Bukkit.getLogger().info("Stopping Foot Race game");
    }
    
    private void setPlayersToAdventure() {
        for (Player participant : participants) {
            participant.setGameMode(GameMode.ADVENTURE);
        }
    }
    
    private void cancelAllTasks() {
        Bukkit.getScheduler().cancelTask(startCountDownTaskID);
        Bukkit.getScheduler().cancelTask(endRaceCountDownId);
        Bukkit.getScheduler().cancelTask(timerRefreshTaskId);
        Bukkit.getScheduler().cancelTask(statusEffectsTaskId);
    }
    
    private void giveBoots() {
        for (Player participant : participants) {
            Color teamColor = gameManager.getTeamColor(participant.getUniqueId());
            ItemStack boots = new ItemStack(Material.LEATHER_BOOTS);
            LeatherArmorMeta meta = (LeatherArmorMeta) boots.getItemMeta();
            meta.setColor(teamColor);
            boots.setItemMeta(meta);
            participant.getEquipment().setBoots(boots);
        }
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
    
    private void clearInventories() {
        for (Player participant : participants) {
            participant.getInventory().clear();
        }
    }
    
    private void clearStatusEffects() {
        for (Player participant : participants) {
            for (PotionEffect effect : participant.getActivePotionEffects()) {
                participant.removePotionEffect(effect.getType());
            }
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
            private int count = 30;
            @Override
            public void run() {
                if (count <= 0) {
                    this.cancel();
                    stop();
                    return;
                }
                for (Player participant : participants) {
                    if (count > 0) {
                        if (count <= 10) {
                            participant.sendMessage(Component.text(count));
                        }
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
                        FastBoard board = boards.get(participant.getUniqueId());
                        if (board != null) {
                            board.updateLine(0, timeString);
                        }
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
    
    private void teleportPlayersToStartingPositions() {
        AnchorManager anchorManager = Main.multiverseCore.getAnchorManager();
        Location anchorLocation = anchorManager.getAnchorLocation("foot-race");
        for (Player participant : participants) {
            participant.sendMessage("Teleporting to Foot Race");
            participant.teleport(anchorLocation);
        }
    }
    
    private void initializeFastBoards() {
        for (Player participant : participants) {
            FastBoard board = new FastBoard(participant);
            board.updateTitle(ChatColor.BLUE+"Foot Race");
            board.updateLines(
                    "00:00:000",
                    "",
                    String.format("Lap: %d/%d", laps.get(participant.getUniqueId()), MAX_LAPS),
                    ""
            );
            boards.put(participant.getUniqueId(), board);
        }
    }
    
    private void hideFastBoards() {
        for (FastBoard board : boards.values()) {
            if (!board.isDeleted()) {
                board.delete();
            }
        }
    }
    
    private void updateFastBoard(UUID playerUniqueId) {
        FastBoard board = boards.get(playerUniqueId);
        long elapsedTime = System.currentTimeMillis() - raceStartTime;
        board.updateLines(
                getTimeString(elapsedTime),
                "",
                String.format("Lap: %d/%d", laps.get(playerUniqueId), MAX_LAPS),
                ""
        );
    }
    
    private void showRaceCompleteFastBoard(Player player) {
        FastBoard board = boards.get(player.getUniqueId());
        long elapsedTime = System.currentTimeMillis() - raceStartTime;
        board.updateLines(
                getTimeString(elapsedTime),
                "",
                "Race Complete!",
                getPlacementTitle(placements.indexOf(player.getUniqueId()) + 1),
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
                updateFastBoard(playerUUID);
                player.sendMessage("Lap " + newLap);
                player.sendMessage(String.format("Finished lap %d in %s", currentLap, getTimeString(elapsedTime)));
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
        showRaceCompleteFastBoard(player);
        int placement = placements.indexOf(player.getUniqueId()) + 1;
        int points = calculatePointsForPlacement(placement);
        gameManager.awardPointsToPlayer(player, points);
        String placementTitle = getPlacementTitle(placement);
        player.sendMessage(String.format("You finished %s! It took you %s", placementTitle, getTimeString(elapsedTime)));
        if (placements.size() == 1) {
            for (Player participant : participants) {
                participant.sendMessage(Component.text(player.getName())
                        .append(Component.text(" finished 1st! Only 30 seconds remain!")));
            }
            startEndRaceCountDown();
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
                return 150;
            case 5:
                return 100;
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
