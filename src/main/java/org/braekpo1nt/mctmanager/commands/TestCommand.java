package org.braekpo1nt.mctmanager.commands;

import org.braekpo1nt.mctmanager.MCTManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class TestCommand implements CommandExecutor {
    
    public TestCommand(MCTManager plugin) {
        plugin.getCommand("test").setExecutor(this);
    }
    
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        
        if (sender instanceof Player) {
            Player p = (Player) sender;
            
            p.sendMessage("Test successful");
        }
        
        return true;
    }
}
