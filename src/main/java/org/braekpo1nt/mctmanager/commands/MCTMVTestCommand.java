package org.braekpo1nt.mctmanager.commands;

import com.onarandombox.MultiverseCore.api.MVWorldManager;
import com.onarandombox.MultiverseCore.api.MultiverseWorld;
import org.braekpo1nt.mctmanager.Main;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import com.onarandombox.MultiverseCore.MultiverseCore;
import org.jetbrains.annotations.NotNull;

/**
 * Delete me. This is an example of how to use the multiverse-core plugin to teleport players to a specific 
 * world or anchor or destination
 */
public class MCTMVTestCommand implements CommandExecutor {
    
    public MCTMVTestCommand(Main plugin) {
        plugin.getCommand("mctmvtest").setExecutor(this);
    }
    
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (sender instanceof Player) {
            Player player = ((Player) sender);
            player.sendMessage("Multiverse Teleport engaged with MCTManager");
            
            Plugin multiversePlugin = Bukkit.getPluginManager().getPlugin("Multiverse-Core");
            MultiverseCore multiverseCore = ((MultiverseCore) multiversePlugin);
            MVWorldManager worldManager = multiverseCore.getMVWorldManager();
            MultiverseWorld FTworld = worldManager.getMVWorld("FT");
            player.teleport(FTworld.getSpawnLocation());
        }
        return true;
    }
}
