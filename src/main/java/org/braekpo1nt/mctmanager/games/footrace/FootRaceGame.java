 package org.braekpo1nt.mctmanager.games.footrace;

import com.onarandombox.MultiverseCore.MultiverseCore;
import com.onarandombox.MultiverseCore.api.MVWorldManager;
import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.Main;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scoreboard.*;
import org.bukkit.util.BoundingBox;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

 /**
  * Handles all the Foot Race game logic. 
  */
 public class FootRaceGame implements Listener {
    
    private final int MAX_LAPS = 3;
    
    private boolean gameActive = false;
    /**
     * Holds the Foot Race world
     */
    private final World footRaceWorld;
    private final BoundingBox finishLine = new BoundingBox(2396, 80, 295, 2404, 79, 308);
    private List<Player> participants;
    private Map<Player, Long> lapCooldowns;
    private Map<Player, Integer> laps;
    private final ScoreboardManager scoreboardManager;
    
    public FootRaceGame(Main plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    
        scoreboardManager = Bukkit.getScoreboardManager();
        Plugin multiversePlugin = Bukkit.getPluginManager().getPlugin("Multiverse-Core");
        MultiverseCore multiverseCore = ((MultiverseCore) multiversePlugin);
        MVWorldManager worldManager = multiverseCore.getMVWorldManager();
        this.footRaceWorld = worldManager.getMVWorld("NT").getCBWorld();
    }
    
    public void start(List<Player> participants) {
        gameActive = true;
        this.participants = participants;
        
        lapCooldowns = participants.stream().collect(
                Collectors.toMap(participant -> participant, key -> System.currentTimeMillis()));
        laps = participants.stream().collect(Collectors.toMap(participant -> participant, key -> 1));
        setupScoreboard();
        
        Bukkit.getLogger().info("Starting Foot Race game");
    }

    private void setupScoreboard() {
        
        for (Player participant :  participants) {
            updateParticipantScoreboard(participant);
        }
    }
    
    private void updateParticipantScoreboard(Player participant) {
        Scoreboard scoreboard = scoreboardManager.getNewScoreboard();
        Objective objective = scoreboard.registerNewObjective("footrace", Criteria.DUMMY, Component.text(ChatColor.BLUE + "Foot Race"));
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        
        Score score = objective.getScore(String.format("Lap: %d/%d", laps.get(participant), MAX_LAPS));
        score.setScore(1);
        
        participant.setScoreboard(scoreboard);
    }

    public void stop() {
        gameActive = false;
        hideScoreboard();
        Bukkit.getLogger().info("Stopping Foot Race game");
    }
    
    private void hideScoreboard() {
        for (Player participant :  participants) {
            participant.setScoreboard(scoreboardManager.getMainScoreboard());
        }
    }

    @EventHandler
    public void onPlayerCrossFinishLine(PlayerMoveEvent event) {
        if (gameActive) {
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
                Bukkit.getLogger().info(String.format("currentLap: %s", currentLap));
                if (currentLap <= 2) {
                    int newLap = currentLap + 1;
                    laps.put(player, newLap);
                    updateParticipantScoreboard(player);
                    player.sendMessage("Lap " + newLap);
                    return;
                }
                int finalLap = 3;
                if (currentLap == finalLap) {
                    laps.put(player, currentLap + 1);
                    player.sendMessage("You finished all 3 laps!");
                }
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
