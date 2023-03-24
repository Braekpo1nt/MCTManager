package org.braekpo1nt.mctmanager.games.footrace;

import com.onarandombox.MultiverseCore.api.MVWorldManager;
import com.onarandombox.MultiverseCore.api.MultiverseWorld;
import com.onarandombox.MultiverseCore.utils.AnchorManager;
import fr.mrmicky.fastboard.FastBoard;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.braekpo1nt.mctmanager.games.MCTGame;
import org.bukkit.*;
import org.bukkit.block.structure.Mirror;
import org.bukkit.block.structure.StructureRotation;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.*;
import org.bukkit.structure.Structure;
import org.bukkit.util.BoundingBox;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Handles all the Foot Race game logic.
 */
public class FootRaceGame implements Listener, MCTGame {
    
    private final int MAX_LAPS = 3;
        
    private boolean gameActive = false;
    /**
     * Holds the Foot Race world
     */
    private final World footRaceWorld;
    private final BoundingBox finishLine = new BoundingBox(2396, 80, 295, 2404, 79, 308);
    private final ScoreboardManager scoreboardManager;
    private final Main plugin;
    private final GameManager gameManager;
    private int startCountDownTaskID;
    private List<Player> participants;
    private Map<Player, Long> lapCooldowns;
    private Map<Player, Integer> laps;
    private ArrayList<Player> placements;
    private boolean raceHasStarted = false;
    private long raceStartTime;
    private final Map<UUID, FastBoard> boards = new HashMap<>();
    
    public FootRaceGame(Main plugin, GameManager gameManager) {
        this.plugin = plugin;
        this.gameManager = gameManager;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        scoreboardManager = Bukkit.getScoreboardManager();
        MVWorldManager worldManager = Main.multiverseCore.getMVWorldManager();
        this.footRaceWorld = worldManager.getMVWorld("NT").getCBWorld();
    }
    
    public void start(List<Player> participants) {
        this.participants = participants;
        
        lapCooldowns = participants.stream().collect(
                Collectors.toMap(participant -> participant, key -> System.currentTimeMillis()));
        laps = participants.stream().collect(Collectors.toMap(participant -> participant, key -> 1));
        placements = new ArrayList<>();
        initializeFastBoards();
        teleportPlayersToStartingPositions();
        giveParticipantsStatusEffects();
        startCountdown();
        
        gameActive = true;
        Bukkit.getLogger().info("Starting Foot Race game");
    }
    
    public void stop() {
        closeGlassBarrier();
        hideFastBoards();
        removeParticipantStatusEffects();
        teleportPlayersToHub();
        stopCountDown();
        raceHasStarted = false;
        gameActive = false;
        Bukkit.getLogger().info("Stopping Foot Race game");
    }
    
    private void giveParticipantsStatusEffects() {
        PotionEffect speed = new PotionEffect(PotionEffectType.SPEED, 10000, 8, true, false, false);
        PotionEffect invisibility = new PotionEffect(PotionEffectType.INVISIBILITY, 10000, 1, true, false, false);
        for (Player participant : participants) {
            participant.addPotionEffect(speed);
            participant.addPotionEffect(invisibility);
        }
    }
    
    private void removeParticipantStatusEffects() {
        for (Player participant : participants) {
            participant.removePotionEffect(PotionEffectType.SPEED);
            participant.removePotionEffect(PotionEffectType.INVISIBILITY);
        }
    }
    
    private void startCountdown() {
        this.startCountDownTaskID = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
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
                    return;
                }
                count--;
            }
        }, 0L, 20L);
    }
    
    private void startRace() {
        openGlassBarrier();
        stopCountDown();
        raceStartTime = System.currentTimeMillis();
        raceHasStarted = true;
    }
    
    private void openGlassBarrier() {
        Structure structure = Bukkit.getStructureManager().loadStructure(new NamespacedKey("mctstructures", "footrace/gateopen"));
        structure.place(new Location(footRaceWorld, 2397, 76, 317), true, StructureRotation.NONE, Mirror.NONE, 0, 1, new Random());
    }
    
    private void closeGlassBarrier() {
        Structure structure = Bukkit.getStructureManager().loadStructure(new NamespacedKey("mctstructures", "footrace/gateclosed"));
        structure.place(new Location(footRaceWorld, 2397, 76, 317), true, StructureRotation.NONE, Mirror.NONE, 0, 1, new Random());
    }
    
    private void stopCountDown() {
        Bukkit.getScheduler().cancelTask(startCountDownTaskID);
    }
    
    private void teleportPlayersToStartingPositions() {
        AnchorManager anchorManager = Main.multiverseCore.getAnchorManager();
        Location anchorLocation = anchorManager.getAnchorLocation("foot-race");
        for (Player participant : participants) {
            participant.sendMessage("Teleporting to Foot Race");
            participant.teleport(anchorLocation);
        }
    }
    
    private void teleportPlayersToHub() {
        MVWorldManager worldManager = Main.multiverseCore.getMVWorldManager();
        MultiverseWorld hubWorld = worldManager.getMVWorld("Hub");
        for (Player participant : participants) {
            participant.sendMessage("Teleporting to Hub");
            participant.teleport(hubWorld.getSpawnLocation());
        }
    }
    
    private void initializeFastBoards() {
        for (Player participant : participants) {
            FastBoard board = new FastBoard(participant);
            board.updateTitle(ChatColor.BLUE+"Foot Race");
            board.updateLines(
                    "",
                    String.format("Lap: %d/%d", laps.get(participant), MAX_LAPS),
                    ""
            );
            boards.put(participant.getUniqueId(), board);
        }
    }
    
    private void hideFastBoards() {
        for (FastBoard board : boards.values()) {
            board.delete();
        }
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
        if (!participants.contains(player)) {
            return;
        }
        if (!player.getWorld().equals(footRaceWorld)) {
            return;
        }
        
        if (isInFinishLineBoundingBox(player)) {
            long lastMoveTime = lapCooldowns.get(player);
            long currentTime = System.currentTimeMillis();
            long coolDownTime = 3000L; // 3 second
            if (currentTime - lastMoveTime < coolDownTime) {
                //Not enough time has elapsed, return without doing anything
                return;
            }
            lapCooldowns.put(player, System.currentTimeMillis());
            
            int currentLap = laps.get(player);
            if (currentLap < MAX_LAPS) {
                long elapsedTime = System.currentTimeMillis() - raceStartTime;
                int newLap = currentLap + 1;
                laps.put(player, newLap);
                updateFastBoard(player);
                player.sendMessage("Lap " + newLap);
                player.sendMessage(String.format("It has been %d seconds", elapsedTime/1000));
                return;
            }
            if (currentLap == MAX_LAPS) {
                laps.put(player, currentLap + 1);
                onPlayerFinishedRace(player);
            }
        }
    }
    
    private void updateFastBoard(Player player) {
        FastBoard board = boards.get(player.getUniqueId());
        board.updateLines(
                "",
                String.format("Lap: %d/%d", laps.get(player), MAX_LAPS),
                ""
        );
    }
    
    private void showRaceCompleteFastBoard(Player player) {
        FastBoard board = boards.get(player.getUniqueId());
        board.updateLines(
                "",
                "Race Complete!",
                ""
        );
    }
    
    /**
     * Code to run when a single player crosses the finish line for the last time
     * @param player The player who crossed the finish line
     */
    private void onPlayerFinishedRace(Player player) {
        long elapsedTime = System.currentTimeMillis() - raceStartTime;
        placements.add(player);
        showRaceCompleteFastBoard(player);
        int placement = placements.indexOf(player) + 1;
        int points = calculatePointsForPlacement(placement);
        try {
            gameManager.awardPointsToPlayer(player, points);
            String placementTitle = getPlacementTitle(placement);
            player.sendMessage(String.format("You finished %s! It took you %d seconds", placementTitle, elapsedTime /1000));
        } catch (IOException e) {
            player.sendMessage(
                    Component.text("Critical error occurred. Please notify an admin to check the logs.")
                    .color(NamedTextColor.RED)
                    .decorate(TextDecoration.BOLD));
            Bukkit.getLogger().severe("Error while adding points to player. See log for error message.");
            throw new RuntimeException(e);
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
