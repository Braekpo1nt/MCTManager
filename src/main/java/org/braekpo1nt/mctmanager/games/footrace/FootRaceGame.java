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

public class FootRaceGame implements Listener {

    private boolean gameActive = false;
    /**
     * Holds the Foot Race world
     */
    private final World footRaceWorld;
    private final BoundingBox finishLine = new BoundingBox(2396, 80, 295, 2404, 79, 308);
    private final List<Player> participants;
    private final Map<Player, Long> lapCooldowns;
    private final Map<Player, Integer> laps;
    
    public FootRaceGame(Main plugin, List<Player> participants) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        this.participants = participants;
        
        lapCooldowns = participants.stream().collect(
                Collectors.toMap(participant -> participant, key -> System.currentTimeMillis()));
        laps = participants.stream().collect(Collectors.toMap(participant -> participant, key -> 1));
        
        Plugin multiversePlugin = Bukkit.getPluginManager().getPlugin("Multiverse-Core");
        MultiverseCore multiverseCore = ((MultiverseCore) multiversePlugin);
        MVWorldManager worldManager = multiverseCore.getMVWorldManager();
        this.footRaceWorld = worldManager.getMVWorld("NT").getCBWorld();
    }
    
    public void start() {
        gameActive = true;
        
        ScoreboardManager manager = Bukkit.getScoreboardManager();
        Scoreboard scoreboard = manager.getNewScoreboard();
        
        Objective objective = scoreboard.registerNewObjective("test", Criteria.DUMMY, Component.text(ChatColor.BLUE + "Title"));
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        
        Score score = objective.getScore(ChatColor.GOLD + "Money: $" + ChatColor.GREEN + 1);
        score.setScore(3);

        for (Player participant :  participants) {
            participant.setScoreboard(scoreboard);
        }
        
        Bukkit.getLogger().info("Starting Foot Race game");
    }
    
    public void stop() {
        gameActive = false;
        hideScoreboard();
        Bukkit.getLogger().info("Stopping Foot Race game");
    }
    
    private void hideScoreboard() {
        for (Player participant :  participants) {
            participant.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
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
                    player.sendMessage("Lap " + newLap);
                    laps.put(player, newLap);
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
