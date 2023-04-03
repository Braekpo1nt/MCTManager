package org.braekpo1nt.mctmanager.commands;

import com.onarandombox.MultiverseCore.api.MVWorldManager;
import org.braekpo1nt.mctmanager.Main;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * A utility command for testing various things, so I don't have to create a new command. 
 */
public class MCTDebugCommand implements CommandExecutor {
    
    private final Main plugin;
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
//        Component mainTitle = Component.text("Main title");
//        Component subTitle = Component.text("Subtitle");
//
//        Title.Times times = Title.Times.times(Duration.ofMillis(500), Duration.ofSeconds(3), Duration.ofMillis(500));
//        Title title = Title.title(mainTitle, subTitle, times);
//        sender.showTitle(title);
        return true;
    }
    
    
    
    
    
}
