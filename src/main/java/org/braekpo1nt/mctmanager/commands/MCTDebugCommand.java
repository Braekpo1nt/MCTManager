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
    
    public MCTDebugCommand(Main plugin) {
        this.plugin = plugin;
        plugin.getCommand("mctdebug").setExecutor(this);
    }
    
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be run by a player.");
            return true;
        }
        Player player = ((Player) sender).getPlayer();
        Location location = player.getLocation();
        Block block = location.getBlock();
        block.setType(Material.CHEST);
        Chest chest = (Chest) block.getState();
        LootTable lootTable = Bukkit.getLootTable(new NamespacedKey("mctdatapack", "mecha/spawn-chest"));
        chest.setLootTable(lootTable);
        chest.update();
        
        
//        if (args.length < 1) {
//            sender.sendMessage("usage: /mctdebug [on|off]");
//            return true;
//        }
//        Component mainTitle = Component.text("Main title");
//        Component subTitle = Component.text("Subtitle");
//
//        Title.Times times = Title.Times.times(Duration.ofMillis(500), Duration.ofSeconds(3), Duration.ofMillis(500));
//        Title title = Title.title(mainTitle, subTitle, times);
//        sender.showTitle(title);
        
        return true;
    }
    
    
}
