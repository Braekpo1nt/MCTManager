package org.braekpo1nt.mctmanager.commands;

import com.onarandombox.MultiverseCore.api.MVWorldManager;
import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.Main;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

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
//        Player player = ((Player) sender).getPlayer();
        
//        Component mainTitle = Component.text("Main title");
//        Component subTitle = Component.text("Subtitle");
//
//        Title.Times times = Title.Times.times(Duration.ofMillis(500), Duration.ofSeconds(3), Duration.ofMillis(500));
//        Title title = Title.title(mainTitle, subTitle, times);
//        sender.showTitle(title);
        return true;
    }
    
    
    public static int getSlotIndex(int line, int column) {
        int slotIndex = (line - 1) * 9 + (column - 1);
        return slotIndex;
    }
    
    
    
}
