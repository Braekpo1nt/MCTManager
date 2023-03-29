package org.braekpo1nt.mctmanager.hub;

import com.onarandombox.MultiverseCore.api.MVWorldManager;
import com.onarandombox.MultiverseCore.api.MultiverseWorld;
import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.Main;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;

public class HubManager implements Listener {
    
    private final PotionEffect RESISTANCE = new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 70, 200, true, false, false);
    private final PotionEffect REGENERATION = new PotionEffect(PotionEffectType.REGENERATION, 70, 200, true, false, false);
    private final PotionEffect NIGHT_VISION = new PotionEffect(PotionEffectType.NIGHT_VISION, 300, 3, true, false, false);
    private final PotionEffect FIRE_RESISTANCE = new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 70, 1, true, false, false);
    private final PotionEffect SATURATION = new PotionEffect(PotionEffectType.SATURATION, 70, 250, true, false, false);
    private final World hubWorld;
    private final Main plugin;
    
    public HubManager(Main plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        MVWorldManager worldManager = Main.multiverseCore.getMVWorldManager();
        this.hubWorld = worldManager.getMVWorld("Hub").getCBWorld();
        initializedStatusEffectLoop();
    }
    
    public void startReturnToHub(List<Player> players) {
        startDelayedReturnToHubTask(players);
    }
    
    private void startDelayedReturnToHubTask(List<Player> players) {
       new BukkitRunnable() {
            private int count = 10;
            @Override
            public void run() {
                if (count <= 0) {
                    returnToHub(players);
                    this.cancel();
                    return;
                }
                for (Player player : players) {
                    player.sendMessage(Component.text("Teleporting to hub in ")
                            .append(Component.text(count)));
                }
                count--;
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }
    
    private void returnToHub(List<Player> players) {
        clearStatusEffects(players);
        teleportPlayersToHub(players);
        clearInventories(players);
        for (Player player : players) {
            giveAmbientStatusEffects(player);
        }
    }
    
    private void clearInventories(List<Player> players) {
        for (Player participant : players) {
            participant.getInventory().clear();
        }
    }
    
    private void teleportPlayersToHub(List<Player> players) {
        MVWorldManager worldManager = Main.multiverseCore.getMVWorldManager();
        MultiverseWorld hubWorld = worldManager.getMVWorld("Hub");
        for (Player participant : players) {
            participant.sendMessage("Teleporting to Hub");
            participant.teleport(hubWorld.getSpawnLocation());
        }
    }
    
    private void clearStatusEffects(List<Player> players) {
        for(Player participant : players) {
            for (PotionEffect effect : participant.getActivePotionEffects()) {
                participant.removePotionEffect(effect.getType());
            }
        }
    }
    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (!player.getWorld().equals(this.hubWorld)) {
            return;
        }
        giveAmbientStatusEffects(player);
    }
    
    private void giveAmbientStatusEffects(Player player) {
        player.addPotionEffect(RESISTANCE);
        player.addPotionEffect(REGENERATION);
        player.addPotionEffect(NIGHT_VISION);
        player.addPotionEffect(FIRE_RESISTANCE);
        player.addPotionEffect(SATURATION);
    }
    
    private void initializedStatusEffectLoop() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for(Player player : Bukkit.getOnlinePlayers()) {
                    if (player.getWorld().equals(hubWorld)) {
                        giveAmbientStatusEffects(player);
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 60L);
    }
    
}
