package org.braekpo1nt.mctmanager.commands;

import com.onarandombox.MultiverseCore.MultiverseCore;
import com.onarandombox.MultiverseCore.api.MVWorldManager;
import com.onarandombox.MultiverseCore.api.MultiverseWorld;
import org.braekpo1nt.mctmanager.Main;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

/**
 * A utility command for testing various things, so I don't have to create a new command. 
 */
public class MCTDebugCommand implements CommandExecutor {
    
    public MCTDebugCommand(Main plugin) {
        plugin.getCommand("mctdebug").setExecutor(this);
    }
    
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be run by a player.");
            return true;
        }
        
        Player player = (Player) sender;
        if (args.length == 1) {
            String worldName = args[0];
            Plugin multiversePlugin = Bukkit.getPluginManager().getPlugin("Multiverse-Core");
            MultiverseCore multiverseCore = ((MultiverseCore) multiversePlugin);
            MVWorldManager worldManager = multiverseCore.getMVWorldManager();
            MultiverseWorld multiverseWorld = worldManager.getMVWorld(worldName);
            if (multiverseWorld == null) {
                player.sendMessage(String.format("%s is not a recognized world name", worldName));
            } else {
                player.sendMessage("Success! " + multiverseWorld.toString());
            }
        }
        return true;
    }
}
