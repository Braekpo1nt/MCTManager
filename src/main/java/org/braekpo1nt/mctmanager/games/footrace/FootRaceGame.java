package org.braekpo1nt.mctmanager.games.footrace;

import com.onarandombox.MultiverseCore.api.MVWorldManager;
import com.onarandombox.MultiverseCore.api.MultiverseWorld;
import com.onarandombox.MultiverseCore.utils.AnchorManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
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
        initializeScoreboard();
        teleportPlayersToStartingPositions();
        giveParticipantsStatusEffects();
        startCountdown();
        
        gameActive = true;
        Bukkit.getLogger().info("Starting Foot Race game");
    }
    
    public void stop() {
        closeGlassBarrier();
        hideScoreboard();
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
    
    private void initializeScoreboard() {
        for (Player participant : participants) {
            updateParticipantScoreboard(participant);
        }
    }
    
    private void updateParticipantScoreboard(Player participant) {
        Scoreboard scoreboard = scoreboardManager.getNewScoreboard();
        Objective objective = scoreboard.registerNewObjective("footrace", Criteria.DUMMY,
                Component.text("Foot Race")
                        .color(NamedTextColor.BLUE));
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        
        Score score = objective.getScore(String.format("Lap: %d/%d", laps.get(participant), MAX_LAPS));
        score.setScore(1);
        
        participant.setScoreboard(scoreboard);
    }
    
    private void displayRaceCompletedScoreboard(Player participant) {
        Scoreboard scoreboard = scoreboardManager.getNewScoreboard();
        Objective objective = scoreboard.registerNewObjective("footrace", Criteria.DUMMY,
                Component.text("Foot Race")
                        .color(NamedTextColor.BLUE));
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        
        Score score = objective.getScore("Race Complete!");
        score.setScore(1);
        
        participant.setScoreboard(scoreboard);
    }
    
    private void hideScoreboard() {
        for (Player participant : participants) {
            participant.setScoreboard(scoreboardManager.getMainScoreboard());
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
            long elapsedTime = System.currentTimeMillis() - raceStartTime;
            if (currentLap < MAX_LAPS) {
                int newLap = currentLap + 1;
                laps.put(player, newLap);
                updateParticipantScoreboard(player);
                player.sendMessage("Lap " + newLap);
                player.sendMessage(String.format("It has been %d seconds", elapsedTime/1000));
                return;
            }
            if (currentLap == MAX_LAPS) {
                laps.put(player, currentLap + 1);
                placements.add(player);
                displayRaceCompletedScoreboard(player);
                int placement = placements.indexOf(player) + 1;
                String placementTitle = getPlacementTitle(placement);
                player.sendMessage(String.format("You finished %s! It took you %d seconds", placementTitle, elapsedTime/1000));
            }
        }
    }
    
    /**
     * Returns the formal placement title of the given place. 
     * 1 gives 1st, 2 gives second, 11 gives 11th, 103 gives 103rd.
     * @param placement A number representing the placement
     * @return The placement number with the appropriate postfix (st, nd, rd, th)
     */
    public static String getPlacementTitle(int placement) {
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
    
    public boolean isGameActive() {
        return gameActive;
    }
}
