package org.braekpo1nt.mctmanager.commands;

import com.onarandombox.MultiverseCore.api.MVWorldManager;
import com.onarandombox.MultiverseCore.api.MultiverseWorld;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.braekpo1nt.mctmanager.Main;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.structure.Mirror;
import org.bukkit.block.structure.StructureRotation;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.loot.LootTable;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.structure.Structure;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.Random;

/**
 * A utility command for testing various things, so I don't have to create a new command. 
 */
public class MCTDebugCommand implements CommandExecutor {
    
    private final Main plugin;
    private int boarderShrinkingTaskId;
    private final WorldBorder worldBorder;
    
    public MCTDebugCommand(Main plugin) {
        this.plugin = plugin;
        plugin.getCommand("mctdebug").setExecutor(this);
        MVWorldManager worldManager = Main.multiverseCore.getMVWorldManager();
        World mechaWorld = worldManager.getMVWorld("FT").getCBWorld();
        this.worldBorder = mechaWorld.getWorldBorder();
    }
    
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be run by a player.");
            return true;
        }
//        Player player = ((Player) sender).getPlayer();
        if (args.length < 1) {
            sender.sendMessage("usage: /mctdebug [on|off]");
            return true;
        }
        
        switch (args[0]) {
            case "on":
                worldBorder.setCenter(0, 0);
                worldBorder.setSize(248);
                kickOffBoarderShrinking();
                return true;
            case "off":
                Bukkit.getScheduler().cancelTask(boarderShrinkingTaskId);
                worldBorder.reset();
                return true;
            default:
                sender.sendMessage("Not recognized option: " + args[0]);
        }
        
//        Component mainTitle = Component.text("Main title");
//        Component subTitle = Component.text("Subtitle");
//
//        Title.Times times = Title.Times.times(Duration.ofMillis(500), Duration.ofSeconds(3), Duration.ofMillis(500));
//        Title title = Title.title(mainTitle, subTitle, times);
//        sender.showTitle(title);
        
        return true;
    }
    
    private void kickOffBoarderShrinking() {
//        int[] sizes = new int[]{180, 150, 100, 50, 25, 2};
//        int[] delays = new int[]{90, 70, 60, 80, 60, 30};
//        int[] durations = new int[]{25,0, 100, 50, 25, 2};
        int[] sizes = new int[]{180, 150, 100};
        int[] delays = new int[]{11, 10, 9};
        int[] durations = new int[]{15, 10, 5};
        this.boarderShrinkingTaskId = new BukkitRunnable() {
            int delay = 0;
            int duration = 0;
            boolean onDelay = false;
            boolean onDuration = false;
            int sceneIndex = 0;
            @Override
            public void run() {
                if (onDelay) {
                    Bukkit.getLogger().info(String.format("Delaying %d/%d", delay, delays[sceneIndex]));
                    if (delay <= 0) {
                        onDelay = false;
                        onDuration = true;
                        duration = durations[sceneIndex];
                        int size = sizes[sceneIndex];
                        worldBorder.setSize(size, duration);
                        return;
                    }
                    delay--;
                } else if (onDuration) {
                    Bukkit.getLogger().info(String.format("Shrinking to %d, %d/%d", sizes[sceneIndex], duration, durations[sceneIndex]));
                    if (duration <= 0) {
                        onDuration = false;
                        onDelay = true;
                        sceneIndex++;
                        if (sceneIndex >= delays.length) {
                            Bukkit.getLogger().info("Boarder is done shrinking");
                            this.cancel();
                            return;
                        }
                        delay = delays[sceneIndex];
                        return;
                    }
                    duration--;
                } else {
                    //initialize
                    onDelay = true;
                    delay = delays[0];
                }
            }
        }.runTaskTimer(plugin, 0L, 20L).getTaskId();
    }
    
    
}
