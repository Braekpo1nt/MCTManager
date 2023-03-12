package org.braekpo1nt.mctmanager.games.footrace;

import com.onarandombox.MultiverseCore.MultiverseCore;
import com.onarandombox.MultiverseCore.api.MVWorldManager;
import org.braekpo1nt.mctmanager.Main;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.BoundingBox;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class FootRaceGame implements Listener {

    private boolean gameActive = true;
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
        laps = participants.stream().collect(Collectors.toMap(participant -> participant, key -> 0));
        
        Plugin multiversePlugin = Bukkit.getPluginManager().getPlugin("Multiverse-Core");
        MultiverseCore multiverseCore = ((MultiverseCore) multiversePlugin);
        MVWorldManager worldManager = multiverseCore.getMVWorldManager();
        this.footRaceWorld = worldManager.getMVWorld("NT").getCBWorld();
    }
    
    public void start() {
        gameActive = true;
        Bukkit.getLogger().info("Starting Foot Race game");
    }

    public void stop() {
        gameActive = false;
        Bukkit.getLogger().info("Stopping Foot Race game");
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
            if (finishLine.contains(player.getLocation().toVector())) {
                long lastMoveTime = lapCooldowns.get(player);
                long currentTime = System.currentTimeMillis();
                long coolDownTime = 3000L; // 3 second
                if (currentTime - lastMoveTime < coolDownTime) {
                    //Not enough time has elapsed, return without doing anything
                    return;
                }
                lapCooldowns.put(player, System.currentTimeMillis());
                int lastLap = laps.get(player);
                int currentLap = lastLap + 1;
                if (currentLap < 3) {
                    laps.put(player, currentLap);
                    player.sendMessage("Lap " + currentLap);
                    return;
                }
                player.sendMessage("You finished all 3 laps!");
            }
        }
    }

    public boolean isGameActive() {
        return gameActive;
    }
}
